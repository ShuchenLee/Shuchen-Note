--get all args
local key = KEYS[1]
local fansId = ARGV[1]
local timestamp = ARGV[2]
-- 使用 EXISTS 命令检查 ZSET 是否存在
local exists = redis.call('EXISTS', key)
if exists == 0 then
    return -1
end
local size = redis.call('ZCARD', key)
if size >= 5000 then
    redis.call('ZPOPMIN', key)
end
--add new fan
redis.call("ZADD",key,timestamp,fansId)
return 0;