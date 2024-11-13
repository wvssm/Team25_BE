package com.team25.backend.domain.manager.service;

import com.team25.backend.domain.manager.dto.request.ManagerCommentUpdateRequest;
import com.team25.backend.domain.manager.dto.request.ManagerCreateRequest;
import com.team25.backend.domain.manager.dto.request.ManagerLocationUpdateRequest;
import com.team25.backend.domain.manager.dto.request.ManagerProfileImageUpdateRequest;
import com.team25.backend.domain.manager.dto.request.ManagerWorkingHourUpdateRequest;
import com.team25.backend.domain.manager.dto.response.ManagerByDateAndRegionResponse;
import com.team25.backend.domain.manager.dto.response.ManagerCommentUpdateResponse;
import com.team25.backend.domain.manager.dto.response.ManagerCreateResponse;
import com.team25.backend.domain.manager.dto.response.ManagerLocationUpdateResponse;
import com.team25.backend.domain.manager.dto.response.ManagerNameResponse;
import com.team25.backend.domain.manager.dto.response.ManagerProfileImageUpdateResponse;
import com.team25.backend.domain.manager.dto.response.ManagerProfileResponse;
import com.team25.backend.domain.manager.dto.response.ManagerWorkingHourUpdateResponse;
import com.team25.backend.domain.manager.entity.Manager;
import com.team25.backend.domain.manager.entity.Certificate;
import com.team25.backend.domain.user.entity.User;
import com.team25.backend.domain.manager.entity.WorkingHour;
import com.team25.backend.global.exception.CustomException;
import com.team25.backend.global.exception.ManagerException;
import com.team25.backend.global.exception.ManagerErrorCode;
import com.team25.backend.domain.manager.repository.ManagerRepository;
import com.team25.backend.domain.manager.repository.CertificateRepository;
import com.team25.backend.domain.manager.repository.WorkingHourRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

import static com.team25.backend.global.exception.ErrorCode.MANAGER_ALREADY_EXISTS;
import static com.team25.backend.global.exception.ErrorCode.MANAGER_NOT_FOUND;

@Service
@Slf4j
@RequiredArgsConstructor
public class ManagerService {

    private final ManagerRepository managerRepository;
    private final CertificateRepository certificateRepository;
    private final WorkingHourRepository workingHourRepository;

    public List<ManagerByDateAndRegionResponse> getManagersByDateAndRegion(String date, String region) {
        validateDate(date);

        String regionPrefix = getRegionPrefix(region);
        validateRegion(regionPrefix);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate localDate = LocalDate.parse(date, formatter);
        String dayOfWeek = localDate.getDayOfWeek().toString().toLowerCase();

        List<Manager> managers = managerRepository.findAll().stream()
                .filter(manager -> hasWorkingHoursOnDay(manager.getWorkingHour(), dayOfWeek))
                .filter(manager -> manager.getWorkingRegion().startsWith(regionPrefix))
                .toList();

        return managers.stream()
                .map(ManagerByDateAndRegionResponse::fromEntity)
                .collect(Collectors.toList());
    }

    private boolean hasWorkingHoursOnDay(WorkingHour workingHour, String dayOfWeek) {
        return switch (dayOfWeek) {
            case "monday" ->
                    !("00:00".equals(workingHour.getMonStartTime()) && "00:00".equals(workingHour.getMonEndTime()));
            case "tuesday" ->
                    !("00:00".equals(workingHour.getTueStartTime()) && "00:00".equals(workingHour.getTueEndTime()));
            case "wednesday" ->
                    !("00:00".equals(workingHour.getWedStartTime()) && "00:00".equals(workingHour.getWedEndTime()));
            case "thursday" ->
                    !("00:00".equals(workingHour.getThuStartTime()) && "00:00".equals(workingHour.getThuEndTime()));
            case "friday" ->
                    !("00:00".equals(workingHour.getFriStartTime()) && "00:00".equals(workingHour.getFriEndTime()));
            case "saturday" ->
                    !("00:00".equals(workingHour.getSatStartTime()) && "00:00".equals(workingHour.getSatEndTime()));
            case "sunday" ->
                    !("00:00".equals(workingHour.getSunStartTime()) && "00:00".equals(workingHour.getSunEndTime()));
            default -> false;
        };
    }

    private void validateDate(String date) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate.parse(date, formatter);
        } catch (DateTimeParseException e) {
            throw new ManagerException(ManagerErrorCode.INVALID_DATE_FORMAT);
        }
    }

    private String getRegionPrefix(String region) {
        return region.length() >= 2 ? region.substring(0, 2) : region;
    }


    private void validateRegion(String regionPrefix) {
        if (!regionExists(regionPrefix)) {
            throw new ManagerException(ManagerErrorCode.REGION_NOT_FOUND);
        }
    }

    private boolean regionExists(String region) {
        return List.of("서울",
                "부산", "인천", "대구", "대전",
                "광주", "울산", "세종", "경기",
                "충남", "충북", "전남", "경북",
                "경남", "강원", "전북", "제주").contains(region);
    }

    @Transactional
    public ManagerCreateResponse createManager(User user, ManagerCreateRequest request) {
        if (user.getId() == null) {
            throw new ManagerException(ManagerErrorCode.UNAUTHORIZED);
        }

        if(managerRepository.existsByUserId(user.getId())){
            throw new CustomException(MANAGER_ALREADY_EXISTS);
        }

        validateCreateRequest(request);

        Manager manager = Manager.builder()
                .user(user)
                .managerName(request.name())
                .profileImage(request.profileImage())
                .career(request.career())
                .comment(request.comment())
                .gender(request.gender())
                .isRegistered(false)
                .build();

        WorkingHour workingHour = new WorkingHour();
        workingHour.setManager(manager);
        manager.setWorkingHour(workingHour);

        managerRepository.save(manager);

        Certificate certificate = Certificate.builder()
                .certificateImage(request.certificateImage())
                .manager(manager)
                .build();

        certificateRepository.save(certificate);
        user.setManager(manager);

        return new ManagerCreateResponse();
    }

    private void validateCreateRequest(ManagerCreateRequest request) {
        if (request.name().isEmpty()) {
            throw new ManagerException(ManagerErrorCode.INVALID_INPUT_VALUE);
        }
    }

    public ManagerProfileResponse getManagerProfile(Long managerId) {
        Manager manager = managerRepository.findById(managerId)
                .orElseThrow(() -> new ManagerException(ManagerErrorCode.MANAGER_NOT_FOUND));

        return ManagerProfileResponse.fromEntity(manager);
    }

    public ManagerProfileResponse getManagerProfile(User user) {
        if (user.getId() == null) {
            throw new ManagerException(ManagerErrorCode.UNAUTHORIZED);
        }

        Manager manager = user.getManager();
        if (manager == null) {
            throw new ManagerException(ManagerErrorCode.MANAGER_NOT_FOUND);
        }

        return ManagerProfileResponse.fromEntity(manager);
    }

    public ManagerWorkingHourUpdateResponse updateWorkingHour(User user, ManagerWorkingHourUpdateRequest request) {
        if (user.getId() == null) {
            throw new ManagerException(ManagerErrorCode.UNAUTHORIZED);
        }

        Manager manager = user.getManager();

        if (manager == null) {
            throw new ManagerException(ManagerErrorCode.MANAGER_NOT_FOUND);
        }

        WorkingHour workingHour = manager.getWorkingHour();
        if (workingHour == null) {
            throw new ManagerException(ManagerErrorCode.WORKING_HOUR_NOT_FOUND);
        }

        validateWorkingHourRequest(request);

        workingHour.setMonStartTime(request.monStartTime());
        workingHour.setMonEndTime(request.monEndTime());
        workingHour.setTueStartTime(request.tueStartTime());
        workingHour.setTueEndTime(request.tueEndTime());
        workingHour.setWedStartTime(request.wedStartTime());
        workingHour.setWedEndTime(request.wedEndTime());
        workingHour.setThuStartTime(request.thuStartTime());
        workingHour.setThuEndTime(request.thuEndTime());
        workingHour.setFriStartTime(request.friStartTime());
        workingHour.setFriEndTime(request.friEndTime());
        workingHour.setSatStartTime(request.satStartTime());
        workingHour.setSatEndTime(request.satEndTime());
        workingHour.setSunStartTime(request.sunStartTime());
        workingHour.setSunEndTime(request.sunEndTime());

        workingHourRepository.save(workingHour);

        return ManagerWorkingHourUpdateResponse.fromEntity(workingHour);
    }

    private void validateWorkingHourRequest(ManagerWorkingHourUpdateRequest request) {
        validateWorkingHour(request.monStartTime(), request.monEndTime());
        validateWorkingHour(request.tueStartTime(), request.tueEndTime());
        validateWorkingHour(request.wedStartTime(), request.wedEndTime());
        validateWorkingHour(request.thuStartTime(), request.thuEndTime());
        validateWorkingHour(request.friStartTime(), request.friEndTime());
        validateWorkingHour(request.satStartTime(), request.satEndTime());
        validateWorkingHour(request.sunStartTime(), request.sunEndTime());
    }

    private void validateWorkingHour(String startTime, String endTime) {
        if (!startTime.matches("\\d{2}:\\d{2}")) {
            throw new ManagerException(ManagerErrorCode.INVALID_WORKING_HOUR_FORMAT);
        }
        if (!endTime.matches("\\d{2}:\\d{2}")) {
            throw new ManagerException(ManagerErrorCode.INVALID_WORKING_HOUR_FORMAT);
        }

        LocalTime start = LocalTime.parse(startTime);
        LocalTime end = LocalTime.parse(endTime);
        if (!(start.equals(LocalTime.MIDNIGHT) && end.equals(LocalTime.MIDNIGHT))) {
            if (!start.isBefore(end)) {
                throw new ManagerException(ManagerErrorCode.INVALID_TIME_RANGE);
            }
        }
    }


    public ManagerProfileImageUpdateResponse updateProfileImage(User user, ManagerProfileImageUpdateRequest request) {
        if (user.getId() == null) {
            throw new ManagerException(ManagerErrorCode.UNAUTHORIZED);
        }

        Manager manager = user.getManager();

        if (manager == null) {
            throw new ManagerException(ManagerErrorCode.MANAGER_NOT_FOUND);
        }

        validateProfileImage(request.profileImage());

        manager.setProfileImage(request.profileImage());
        managerRepository.save(manager);

        return ManagerProfileImageUpdateResponse.fromEntity(manager);
    }

    private void validateProfileImage(String profileImage) {
        if (profileImage == null || profileImage.isEmpty()) {
            throw new ManagerException(ManagerErrorCode.INVALID_PROFILE_IMAGE);
        }
    }

    public ManagerCommentUpdateResponse updateComment(User user, ManagerCommentUpdateRequest request) {
        if (user.getId() == null) {
            throw new ManagerException(ManagerErrorCode.UNAUTHORIZED);
        }

        Manager manager = user.getManager();

        if (manager == null) {
            throw new ManagerException(ManagerErrorCode.MANAGER_NOT_FOUND);
        }

        validateComment(request.comment());

        manager.setComment(request.comment());
        managerRepository.save(manager);

        return ManagerCommentUpdateResponse.fromEntity(manager);
    }

    private void validateComment(String comment) {
        if (comment == null || comment.isEmpty()) {
            throw new ManagerException(ManagerErrorCode.INVALID_COMMENT);
        }
    }

    public ManagerLocationUpdateResponse updateLocation(User user, ManagerLocationUpdateRequest request) {
        if (user.getId() == null) {
            throw new ManagerException(ManagerErrorCode.UNAUTHORIZED);
        }

        Manager manager = user.getManager();

        if (manager == null) {
            throw new ManagerException(ManagerErrorCode.MANAGER_NOT_FOUND);
        }

        validateWorkingRegion(request.workingRegion());

        manager.setWorkingRegion(request.workingRegion());
        managerRepository.save(manager);

        return ManagerLocationUpdateResponse.fromEntity(manager);
    }

    private void validateWorkingRegion(String workingRegion) {
        if (workingRegion == null || workingRegion.isEmpty()) {
            throw new ManagerException(ManagerErrorCode.INVALID_WORKING_REGION);
        }
    }

    public ManagerNameResponse findManagerNameByUserId(Long userId){
        Manager manager = managerRepository.findByUserId(userId)
                .orElseThrow(() ->new CustomException(MANAGER_NOT_FOUND));
        return new ManagerNameResponse(manager.getManagerName());
    }

    public List<Manager> getManagersWithCertificatesAndWorkingHour() {
        return managerRepository.findManagersWithCertificatesAndWorkingHour();
    }
}
