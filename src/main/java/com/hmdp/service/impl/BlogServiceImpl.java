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
        if(blog == null){
            //不存在
            return null;
        }
        //查询blog相关用户
        Long userId = blog.getUserId();
        User user = userService.getById(userId);
        blog.setName(user.getNickName());
        blog.setIcon(user.getIcon());
        return null;
    }

    @Override
    public Boolean likeBlog(Long id) {
        //从线程中获取登录用户
        Long userId = UserHolder.getUser().getId();
        //判断用户是否已经点过赞
        String key = "blog:liked:" + id;
        Boolean member = stringRedisTemplate.opsForSet().isMember(key, userId.toString());
        //未点赞，可以点赞
        if(BooleanUtil.isFalse(member)){
            //修改数据库
            boolean isSuccess = update().setSql("liked = liked + 1").eq("id", userId).update();
            if(isSuccess){
                //保存用户到redis的集合里面，说明该用户已经点过赞了
                Long add = stringRedisTemplate.opsForSet().add(key, userId.toString());
            }
        }
        else{
            
        }
    }
}
