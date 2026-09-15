package com.rowing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 组别保存结果。
 * 场务修改竞速距离后，超出支架适配区间的绑定会被当场拆掉，
 * 被拆掉的绑定明细随保存结果一并返回，用于当场告知场务。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupUpdateResultDTO {

    /** 保存后的组别 */
    private GroupDTO group;

    /** 本次保存是否变更了竞速距离 */
    private Boolean distanceChanged;

    /** 变更前竞速距离（未变更时为 null） */
    private Integer previousDistance;

    /** 变更后竞速距离（未变更时为 null） */
    private Integer newDistance;

    /** 因对不上支架适配区间而被拆掉的绑定 */
    private List<UnboundBindingItem> unboundBindings;

    /** 被拆掉的绑定条数 */
    private Integer unboundCount;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UnboundBindingItem {

        private Long bindingId;
        private Long bracketId;
        private String bracketCode;
        private Integer bracketMinDistance;
        private Integer bracketMaxDistance;
        private Long groupId;
        private String groupName;
        private String groupCode;
        private Integer previousDistance;
        private Integer newDistance;
    }
}
