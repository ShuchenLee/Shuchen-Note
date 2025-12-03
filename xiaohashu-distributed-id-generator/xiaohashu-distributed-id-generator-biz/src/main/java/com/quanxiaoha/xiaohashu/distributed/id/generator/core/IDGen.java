package com.quanxiaoha.xiaohashu.distributed.id.generator.core;

import com.quanxiaoha.xiaohashu.distributed.id.generator.core.common.Result;

public interface IDGen {
    Result get(String key);
    boolean init();
}
