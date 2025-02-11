package com.shark.aio.base.diskCapacity;

import lombok.Data;

/**
 * @author lbx
 * @date 2023/3/30 - 19:19
 **/
@Data
public class DiskCapacityEntity {
    private String totalMemorySize;
    private String totalSpace;
    private boolean connect;
    private String usedRAMRate;
    private String useRate;
}
