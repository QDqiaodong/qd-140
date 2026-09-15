package com.rowing.service.impl;

import com.rowing.dto.request.GroupCreateRequest;
import com.rowing.dto.request.GroupUpdateRequest;
import com.rowing.dto.response.GroupDTO;
import com.rowing.dto.response.GroupUpdateResultDTO;
import com.rowing.dto.response.PageResult;
import com.rowing.entity.BindingChangeLog;
import com.rowing.entity.BracketBinding;
import com.rowing.entity.DockingBracket;
import com.rowing.entity.RowingGroup;
import com.rowing.exception.BusinessException;
import com.rowing.repository.BindingChangeLogRepository;
import com.rowing.repository.BracketBindingRepository;
import com.rowing.repository.DockingBracketRepository;
import com.rowing.repository.RowingGroupRepository;
import com.rowing.service.RowingGroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RowingGroupServiceImpl implements RowingGroupService {

    /** 基地允许上传的训练计划附件类型（扩展名白名单，展示顺序） */
    private static final List<String> ALLOWED_EXTENSION_LIST = Arrays.asList(
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt");

    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(ALLOWED_EXTENSION_LIST);

    private static final long MAX_FILENAME_LENGTH = 200L;

    private final RowingGroupRepository groupRepository;
    private final BracketBindingRepository bindingRepository;
    private final DockingBracketRepository bracketRepository;
    private final BindingChangeLogRepository changeLogRepository;

    @Value("${rowing.plan.upload-dir:./uploads/plans}")
    private String planUploadDir;

    @Override
    @Transactional
    public GroupDTO create(GroupCreateRequest request) {
        if (groupRepository.existsByGroupCode(request.getGroupCode())) {
            throw new BusinessException("组别编码已存在");
        }

        RowingGroup group = RowingGroup.builder()
                .groupName(request.getGroupName())
                .groupCode(request.getGroupCode())
                .racingDistance(request.getRacingDistance())
                .description(request.getDescription())
                .build();

        group = groupRepository.save(group);
        log.info("创建组别成功: {}", group.getGroupCode());
        return GroupDTO.fromEntity(group);
    }

    @Override
    @Transactional
    public GroupUpdateResultDTO update(GroupUpdateRequest request) {
        RowingGroup group = groupRepository.findById(request.getId())
                .orElseThrow(() -> new BusinessException("组别不存在"));

        Integer oldDistance = group.getRacingDistance();

        if (request.getGroupName() != null) {
            group.setGroupName(request.getGroupName());
        }
        if (request.getGroupCode() != null && !request.getGroupCode().equals(group.getGroupCode())) {
            if (groupRepository.existsByGroupCode(request.getGroupCode())) {
                throw new BusinessException("组别编码已存在");
            }
            group.setGroupCode(request.getGroupCode());
        }
        if (request.getRacingDistance() != null) {
            group.setRacingDistance(request.getRacingDistance());
        }
        if (request.getDescription() != null) {
            group.setDescription(request.getDescription());
        }

        Integer newDistance = group.getRacingDistance();
        boolean distanceChanged = !oldDistance.equals(newDistance);

        List<GroupUpdateResultDTO.UnboundBindingItem> unboundItems = new ArrayList<>();

        // 竞速距离改完后，对不上支架适配区间的生效绑定当场拆掉，避免悄悄失效
        if (distanceChanged) {
            List<BracketBinding> bindings = bindingRepository.findByGroupId(request.getId());
            for (BracketBinding binding : bindings) {
                if (binding.getStatus() != 1) {
                    continue;
                }
                DockingBracket bracket = bracketRepository.findById(binding.getBracketId()).orElse(null);
                if (bracket == null) {
                    continue;
                }
                if (newDistance < bracket.getMinDistance() || newDistance > bracket.getMaxDistance()) {
                    binding.setStatus(0);
                    bindingRepository.save(binding);

                    BindingChangeLog logEntry = BindingChangeLog.builder()
                            .bracketId(bracket.getId())
                            .bracketCode(bracket.getBracketCode())
                            .groupId(group.getId())
                            .groupName(group.getGroupName())
                            .changeType("UNBIND")
                            .previousDistance(oldDistance)
                            .newDistance(newDistance)
                            .changeReason("组别竞速距离由" + oldDistance + "m改为" + newDistance
                                    + "m，超出支架" + bracket.getMinDistance() + "-"
                                    + bracket.getMaxDistance() + "m适配区间，系统自动解绑")
                            .operator("system")
                            .build();
                    changeLogRepository.save(logEntry);

                    unboundItems.add(GroupUpdateResultDTO.UnboundBindingItem.builder()
                            .bindingId(binding.getId())
                            .bracketId(bracket.getId())
                            .bracketCode(bracket.getBracketCode())
                            .bracketMinDistance(bracket.getMinDistance())
                            .bracketMaxDistance(bracket.getMaxDistance())
                            .groupId(group.getId())
                            .groupName(group.getGroupName())
                            .groupCode(group.getGroupCode())
                            .previousDistance(oldDistance)
                            .newDistance(newDistance)
                            .build());
                }
            }
        }

        group = groupRepository.save(group);
        log.info("更新组别成功: {}，竞速距离 {} -> {}，自动解绑绑定 {} 条",
                group.getGroupCode(), oldDistance, newDistance, unboundItems.size());

        return GroupUpdateResultDTO.builder()
                .group(GroupDTO.fromEntity(group))
                .distanceChanged(distanceChanged)
                .previousDistance(distanceChanged ? oldDistance : null)
                .newDistance(distanceChanged ? newDistance : null)
                .unboundBindings(unboundItems)
                .unboundCount(unboundItems.size())
                .build();
    }

    @Override
    @Transactional
    public void delete(Long id) {
        RowingGroup group = groupRepository.findById(id)
                .orElseThrow(() -> new BusinessException("组别不存在"));

        deletePlanFile(group);
        bindingRepository.deactivateByGroupId(id);
        groupRepository.delete(group);

        log.info("删除组别成功: {}", group.getGroupCode());
    }

    @Override
    public GroupDTO getById(Long id) {
        RowingGroup group = groupRepository.findById(id)
                .orElseThrow(() -> new BusinessException("组别不存在"));
        return GroupDTO.fromEntity(group);
    }

    @Override
    public GroupDTO getByCode(String groupCode) {
        RowingGroup group = groupRepository.findByGroupCode(groupCode)
                .orElseThrow(() -> new BusinessException("组别不存在"));
        return GroupDTO.fromEntity(group);
    }

    @Override
    public PageResult<GroupDTO> query(String groupName, String groupCode, Integer racingDistance, Integer pageNum, Integer pageSize) {
        Pageable pageable = PageRequest.of(
                pageNum - 1,
                pageSize,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<RowingGroup> page = groupRepository.findByConditions(groupName, groupCode, racingDistance, pageable);

        List<GroupDTO> dtoList = page.getContent().stream()
                .map(GroupDTO::fromEntity)
                .collect(Collectors.toList());

        return PageResult.of(dtoList, page.getTotalElements(), pageNum, pageSize);
    }

    @Override
    public List<GroupDTO> findAll() {
        return groupRepository.findAll().stream()
                .map(GroupDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<GroupDTO> findByRacingDistance(Integer distance) {
        return groupRepository.findByRacingDistance(distance).stream()
                .map(GroupDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<Integer> findDistinctRacingDistances() {
        return groupRepository.findDistinctRacingDistances();
    }

    @Override
    @Transactional
    public GroupDTO uploadPlan(Long groupId, MultipartFile file) {
        RowingGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException("组别不存在"));

        if (file == null || file.isEmpty()) {
            throw new BusinessException("训练计划附件不能为空");
        }

        String originalName = StringUtils.cleanPath(
                file.getOriginalFilename() == null ? "" : file.getOriginalFilename());
        String extension = getExtension(originalName);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException("训练计划附件格式不被基地允许，请更换为 "
                    + String.join("、", ALLOWED_EXTENSION_LIST) + " 格式的文件");
        }

        if (originalName.length() > MAX_FILENAME_LENGTH) {
            throw new BusinessException("附件文件名过长，请修改后重新上传");
        }

        Path uploadRoot = Paths.get(planUploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(uploadRoot);
        } catch (IOException e) {
            log.error("创建训练计划存储目录失败: {}", uploadRoot, e);
            throw new BusinessException("附件存储目录不可用，请联系管理员");
        }

        String storedName = "group_" + groupId + "_" + System.currentTimeMillis() + "." + extension;
        Path target = uploadRoot.resolve(storedName).normalize();
        if (!target.startsWith(uploadRoot)) {
            throw new BusinessException("非法的附件文件名");
        }

        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("训练计划附件落盘失败: groupId={}, file={}", groupId, originalName, e);
            deleteStoredFile(storedName, uploadRoot);
            throw new BusinessException("附件上传失败，请稍后重试");
        }

        // 附件已落盘并通过校验，再替换旧附件并保存组别
        String oldFilePath = group.getPlanFilePath();
        group.setPlanFileName(originalName);
        group.setPlanFilePath(storedName);
        group = groupRepository.save(group);
        log.info("组别 {} 上传训练计划成功: {}", group.getGroupCode(), originalName);

        deleteStoredFile(oldFilePath, uploadRoot);

        return GroupDTO.fromEntity(group);
    }

    @Override
    public PlanFile loadPlan(Long groupId) {
        RowingGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException("组别不存在"));

        if (!StringUtils.hasText(group.getPlanFilePath())) {
            throw new BusinessException("该组别尚未上传训练计划附件");
        }

        Path uploadRoot = Paths.get(planUploadDir).toAbsolutePath().normalize();
        Path target = uploadRoot.resolve(group.getPlanFilePath()).normalize();
        if (!target.startsWith(uploadRoot) || !Files.exists(target)) {
            log.error("训练计划附件文件丢失: groupId={}, path={}", groupId, group.getPlanFilePath());
            throw new BusinessException("训练计划附件文件不存在或已被删除");
        }

        try {
            Resource resource = new UrlResource(target.toUri());
            if (!resource.isReadable()) {
                throw new BusinessException("训练计划附件无法读取");
            }
            return new PlanFile(resource, group.getPlanFileName(), Files.size(target));
        } catch (MalformedURLException e) {
            log.error("训练计划附件路径异常: {}", target, e);
            throw new BusinessException("训练计划附件无法读取");
        } catch (IOException e) {
            log.error("训练计划附件读取失败: {}", target, e);
            throw new BusinessException("训练计划附件无法读取");
        }
    }

    private String getExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }

    private void deletePlanFile(RowingGroup group) {
        if (!StringUtils.hasText(group.getPlanFilePath())) {
            return;
        }
        Path uploadRoot = Paths.get(planUploadDir).toAbsolutePath().normalize();
        deleteStoredFile(group.getPlanFilePath(), uploadRoot);
    }

    private void deleteStoredFile(String storedName, Path uploadRoot) {
        if (!StringUtils.hasText(storedName)) {
            return;
        }
        Path target = uploadRoot.resolve(storedName).normalize();
        if (!target.startsWith(uploadRoot)) {
            return;
        }
        try {
            Files.deleteIfExists(target);
        } catch (IOException e) {
            log.warn("删除旧训练计划附件失败: {}", target, e);
        }
    }
}