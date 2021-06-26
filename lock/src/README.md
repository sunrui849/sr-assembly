* 实现RedisOperator并注册成bean
* 在释放锁由于删除不是原子操作，所以理论上存在误删的可能，当threadId可以保证全局唯一性的时候可以避免误删问题
* com.sr.assembly.lock.redis.keyPrefix 配置redis前缀
* com.sr.assembly.lock.redis = true时开启