---
--- Created by LeeShuchen
--- DateTime: 2025/12/13 16:47
---
local redisKey = KEYS[1]
for i = 1,#ARGV-1 do
    redis.call("BF.ADD",redisKey,ARGV[i])
end
local expireSecs = ARGV[#ARGV]
redis.call("EXPIRE",redisKey,expireSecs)
return 0