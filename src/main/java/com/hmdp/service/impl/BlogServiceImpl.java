package com.hmdp.service.impl;

import cn.hutool.core.util.BooleanUtil;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.Blog;
import com.hmdp.entity.User;
import com.hmdp.mapper.BlogMapper;
import com.hmdp.service.IBlogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.service.IUserService;
import com.hmdp.utils.UserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import static com.hmdp.utils.RedisConstants.BLOG_LIKED_KEY;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 陈浩轩
 * @since 2021-12-22
 */

@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements IBlogService {

    @Resource
    private IUserService userService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public Result queryBlogById(Long id) {
        //查询blog
        Blog blog = getById(id);
        if (blog == null) {
            return Result.fail("笔记不存在！");
        }
        //查询blog相关用户
        User user = userService.getById(blog.getUserId());
        blog.setName(user.getNickName());
        blog.setIcon(user.getIcon());
        //查询当前登录用户是否点过赞
        blog.setIsLike(isBlogLiked(blog.getId()));
        return Result.ok(blog);
    }

    private boolean isBlogLiked(Long blogId) {
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            return false;
        }
        Boolean isMember = stringRedisTemplate.opsForSet()
                .isMember(BLOG_LIKED_KEY + blogId, user.getId().toString());
        return BooleanUtil.isTrue(isMember);
    }

    @Override
    public Boolean likeBlog(Long id) {
        //从线程中获取登录用户
        Long userId = UserHolder.getUser().getId();
        String key = BLOG_LIKED_KEY + id;
        //判断用户是否已经点过赞
        Boolean isMember = stringRedisTemplate.opsForSet().isMember(key, userId.toString());
        if (BooleanUtil.isFalse(isMember)) {
            //未点赞，点赞：数据库点赞数+1
            boolean isSuccess = update().setSql("liked = liked + 1").eq("id", id).update();
            if (isSuccess) {
                //保存用户到redis的集合里面，说明该用户已经点过赞了
                stringRedisTemplate.opsForSet().add(key, userId.toString());
            }
            return isSuccess;
        }
        //已点赞，取消点赞：数据库点赞数-1
        boolean isSuccess = update().setSql("liked = liked - 1").eq("id", id).update();
        if (isSuccess) {
            //把用户从redis的集合中移除
            stringRedisTemplate.opsForSet().remove(key, userId.toString());
        }
        return !isSuccess;
    }
}
