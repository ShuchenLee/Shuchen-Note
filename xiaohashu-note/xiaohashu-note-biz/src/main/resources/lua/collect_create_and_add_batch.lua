---
--- Created by LeeShuchen
--- DateTime: 2025/12/13 17:15
---
local redisKey = KEYS[1]
local zaddArgs = {}
for i = 1, #ARGV - 1, 2 do
    table.insert(zaddArgs, ARGV[i])      -- 分数（点赞时间）
    table.insert(zaddArgs, ARGV[i+1])    -- 值（笔记ID）
end
-- 调用 ZADD 批量插入数据
redis.call('ZADD', redisKey, unpack(zaddArgs))
-- 最后一个参数为过期时间
local expireTime = ARGV[#ARGV]
-- 设置过期时间
redis.call("EXPIRE", redisKey, expireTime)
return 0
