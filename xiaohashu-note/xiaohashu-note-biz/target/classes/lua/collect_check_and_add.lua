---Z
--- Created by LeeShuchen
--- DateTime: 2025/12/13 17:28
---
local redisKey = KEYS[1]
local noteId = ARGV[1]
local timeStamp = ARGV[2]
--if collect list exist
local exist = redis.call("EXISTS",redisKey)
if exist == 0 then
    return -1
end
local size = redis.call("ZCARD",redisKey)
if size >= 100 then
    redis.call("ZPOPMIN",redisKey)
end
redis.call("ZADD",redisKey,timeStamp,noteId)
return 0

