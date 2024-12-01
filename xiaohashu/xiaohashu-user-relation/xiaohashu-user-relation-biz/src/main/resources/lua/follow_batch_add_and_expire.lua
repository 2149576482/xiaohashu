-- 操作的 Key
local key = KEYS[1]

-- 准备批量添加数据的参数
local zaddArgs = {}

-- 遍历 ARGV 参数，将分数和值按顺序插入到 zaddArgs 变量中
for i = 1, #ARGV - 1, 2 do
    table.insert(zaddArgs, ARGV[i])      -- 分数（关注时间）
    table.insert(zaddArgs, ARGV[i+1])    -- 值（关注的用户ID）
end

-- 调用 ZADD 批量插入数据
redis.call('ZADD', key, unpack(zaddArgs))

-- 设置 ZSet 的过期时间
local expireTime = ARGV[#ARGV] -- 最后一个参数为过期时间
redis.call('EXPIRE', key, expireTime)

return 0


-- local zaddArgs = {}： 创建一个空表zaddArgs来存储批量添加的数据。
--for i = 1, #ARGV - 1, 2 do ：开始一个循环，遍历ARGV数组（ARGV是从外部传入的参数数组）。循环从第一个元素开始，每次迭代跳过两个元素（一个时间戳和一个用户ID）。
--table.insert(zaddArgs, ARGV[i]) ： 将分数（时间戳）插入到zaddArgs表中。
--table.insert(zaddArgs, ARGV[i+1]) ： 将关注的用户ID插入到zaddArgs表中。这样就保证了每个时间戳后面跟着一个用户ID。
--redis.call('ZADD', key, unpack(zaddArgs)) ：使用ZADD命令将所有关注关系批量添加到有序集合中。unpack函数用于展开zaddArgs表，使其成为多个参数传递给ZADD命令。
--local expireTime = ARGV[#ARGV] ： 获取最后一个参数（ARGV数组的最后一个元素），即过期时间。
--redis.call('EXPIRE', key, expireTime) - 对整个有序集合（Key）设置过期时间。
--return 0 - 返回0表示操作成功。
