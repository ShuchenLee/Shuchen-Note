---
--- Created by LeeShuchen
--- DateTime: 2025/12/12 11:09
---
local key = KEYS[1]
local noteId = ARGV[1]
local timeStamp = ARGV[2]
--if like list exist
local exist = redis.call("EXISTS",key)
if exist == 0 then
    return -1
end
local size = redis.call("ZCARD",key)
if size >= 100 then
    redis.call("ZPOPMIN",key)
end
redis.call("ZADD",key,timeStamp,noteId)
return 0
