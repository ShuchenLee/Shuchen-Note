package com.quanxiaoha.kv.biz;

import com.quanxiaoha.kv.biz.domain.dataobject.NoteContentDO;
import com.quanxiaoha.kv.biz.domain.respsitory.NoteContentRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

@SpringBootTest
@Slf4j
public class CassandraTest {
    @Resource
    private NoteContentRepository noteContentRepository;
    @Test
    public void test1(){
        NoteContentDO nodeContent = NoteContentDO.builder()
                .id(UUID.randomUUID())
                .content("代码测试笔记内容插入")
                .build();

        noteContentRepository.save(nodeContent);
    }
    @Test
    public void testUpdate(){
        NoteContentDO nodeContent = NoteContentDO.builder()
                .id(UUID.fromString("23a6ae34-fbcc-4180-b647-e28e5356e679"))
                .content("这是一条测试更新的内容")
                .build();
        noteContentRepository.save(nodeContent);
    }
    @Test
    public void testDelete(){
        noteContentRepository.deleteById(UUID.fromString("23a6ae34-fbcc-4180-b647-e28e5356e679"));
    }
}
