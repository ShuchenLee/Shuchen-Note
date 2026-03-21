package com.quanxiaoha.xiaohashu.data.align.job;

import com.quanxiaoha.xiaohashu.data.align.constants.TableConstants;
import com.quanxiaoha.xiaohashu.data.align.domain.mapper.CreateTableMapper;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Scheduled Task: Automatically Create Daily Incremental Count Change Tables
 */
@Component
public class CreateTableXxlJob {
    @Value("${table.shards}")
    private int tableShards;
    @Resource
    private CreateTableMapper createTableMapper;

    @XxlJob("CreateTableJobHandler")
    public void createTableJobHandler() throws Exception {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        XxlJobHelper.log("============start to create {} data increment table", date);
        if (tableShards > 0) {
            for (int hashkey = 0; hashkey < tableShards; hashkey++) {
                String tableNameSuffix = TableConstants.buildTableNameSuffix(date, hashkey);
                createTableMapper.createDataAlignFollowingCountTempTable(tableNameSuffix);
                createTableMapper.createDataAlignFansCountTempTable(tableNameSuffix);
                createTableMapper.createDataAlignUserLikeCountTempTable(tableNameSuffix);
                createTableMapper.createDataAlignNoteLikeCountTempTable(tableNameSuffix);
                createTableMapper.createDataAlignNotePublishCountTempTable(tableNameSuffix);
                createTableMapper.createDataAlignUserCollectCountTempTable(tableNameSuffix);
                createTableMapper.createDataAlignNoteCollectCountTempTable(tableNameSuffix);
            }
        }
        XxlJobHelper.log("============create {} data increment table successfully", date);
    }
}
