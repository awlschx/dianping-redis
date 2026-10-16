package com.hmdp.utils;

import cn.hutool.core.codec.PunyCode;

public interface ILock {
    public boolean tryLock(Long timeoutSec);
    public void unlock();
}
