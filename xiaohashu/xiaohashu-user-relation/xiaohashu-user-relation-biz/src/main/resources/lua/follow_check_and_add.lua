-- LUA 脚本：校验并添加关注关系

local key = KEYS[1] -- 操作的 Redis Key
local followUserId = ARGV[1] -- 关注的用户ID
local timestamp = ARGV[2] -- 时间戳

-- 使用 EXISTS 命令检查 KEY 是否存在
local exists = redis.call('EXISTS', key)
if exists == 0 then
    return -1
end

-- 校验关注人数是否上限（是否达到 1000）
local size = redis.call('ZCARD', key)
if size >= 1000 then
    return -2
end

-- 校验目标用户是否已经关注
if redis.call('ZSCORE', key, followUserId) then
    return -3
end

-- ZADD 添加关注关系
redis.call('ZADD', key, timestamp, followUserId)
return 0


-- local key = KEYS[1] : 获取第一个键（通过Redis命令参数传递），这里是存储关注关系的有序集合（ZSet）的Key。
-- local followUserId = ARGV[1] :获取第一个参数，这是要添加的关注对象的用户ID。
-- local timestamp = ARGV[2] ：获取第二个参数，这里是一个时间戳，通常用于排序关注关系的时间顺序。
-- local exists = redis.call('EXISTS', key) ：检查给定的Key是否存在。如果不存在(exists == 0)，则返回一个错误码，这里自定义为 -1, 表示缓存不存在。
-- local size = redis.call('ZCARD', key) ：计算有序集合中的元素数量。如果关注数已经达到或超过1000，则返回-2表示已到达关注上限。
-- if redis.call('ZSCORE', key, followUserId)： 检查指定的用户ID是否已经在有序集合中。如果用户已经被关注，则返回-3表示重复关注。
-- redis.call('ZADD', key, timestamp, followUserId) ： 如果以上条件都满足，那么将新的关注关系添加到有序集合中，使用时间戳作为分数，这有助于之后按照时间顺序进行排序。
-- return 0 - 如果所有操作成功执行，则返回0表示成功。
