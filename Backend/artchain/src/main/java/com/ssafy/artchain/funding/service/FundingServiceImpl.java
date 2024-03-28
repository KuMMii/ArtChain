package com.ssafy.artchain.funding.service;


import com.ssafy.artchain.connectentity.entity.InvestmentLog;
import com.ssafy.artchain.connectentity.repository.InvestmentLogRepository;
import com.ssafy.artchain.funding.dto.*;
import com.ssafy.artchain.funding.entity.*;
import com.ssafy.artchain.funding.repository.*;
import com.ssafy.artchain.member.dto.CustomUserDetails;
import com.ssafy.artchain.member.entity.Member;
import com.ssafy.artchain.member.repository.MemberRepository;
import com.ssafy.artchain.s3.S3Service;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class FundingServiceImpl implements FundingService {

    private final FundingRepository fundingRepository;
    private final FundingNoticeRepository fundingNoticeRepository;
    private final FundingScheduleRepository fundingScheduleRepository;
    private final FundingExpectedReturnRepository fundingExpectedReturnRepository;
    private final FundingSaleRepository fundingSaleRepository;
    private final FundingCostRepository fundingCostRepository;
    private final InvestmentLogRepository investmentLogRepository;
    private final MemberRepository memberRepository;
    private final S3Service s3Service;
    private final EntityManager em;
    private final String ROLE_COMPANY = "ROLE_COMPANY";
    private final String ROLE_ADMIN = "ROLE_ADMIN";
    private final String ROLE_USER = "ROLE_USER";

    @Override
    @Transactional
    public Long createFunding(MultipartFile poster, MultipartFile descriptionImg, FundingCreateRequestDto data, CustomUserDetails member) {
        if (member.getAuthorities().stream().noneMatch(au -> au.getAuthority().equals(ROLE_COMPANY))) {
            return -3L;
        }

        try {
            data.setPoster(s3Service.upload(poster, data.getName()));
            data.setDescriptionImg(s3Service.upload(descriptionImg, data.getName()));
        } catch (IOException e) {
            return -2L;
        }

        Funding funding = fundingRepository.save(Funding.builder()
                .entId(data.getEntId())
                .name(data.getName())
                .poster(data.getPoster())
                .category(data.getCategory())
                .descriptionImg(data.getDescriptionImg())
                .recruitStart(data.getRecruitStart())
                .recruitEnd(data.getRecruitEnd())
                .settlement(data.getSettlement())
                .goalCoinCount(data.getGoalCoinCount())
                .nowCoinCount(0L)
                .contractAddress(data.getContractAddress())
                .totalBudget(data.getTotalBudget())
                .unitPrice(data.getUnitPrice())
                .bep(data.getBep())
                .progressStatus(FundingProgressStatus.BEFORE_RECRUITMENT)
                .isAllow(null)
                .investmentLogs(new ArrayList<>())
                .scheduleList(new ArrayList<>())
                .expectedReturnList(new ArrayList<>())
                .saleList(new ArrayList<>())
                .costList(new ArrayList<>())
                .build());

        for (FundingScheduleCreateDto schedule : data.getScheduleList()) {
            fundingScheduleRepository.save(FundingSchedule.builder()
                    .funding(funding)
                    .scheduleName(schedule.getScheduleName())
                    .scheduleDate(schedule.getScheduleDate())
                    .build());
        }

        for (FundingExpectedReturnCreateDto expectedReturn : data.getExpectedReturnList()) {
            fundingExpectedReturnRepository.save(FundingExpectedReturn.builder()
                    .funding(funding)
                    .spectatorNum(expectedReturn.getSpectatorNum())
                    .expectedReturn(expectedReturn.getExpectedReturn())
                    .build());
        }

        for (FundingSaleCreateDto sale : data.getSaleList()) {
            fundingSaleRepository.save(FundingSale.builder()
                    .funding(funding)
                    .mainVariety(sale.getMainVariety())
                    .subVariety(sale.getSubVariety())
                    .percentage(sale.getPercentage())
                    .build());
        }

        for (FundingCostCreateDto cost : data.getCostList()) {
            fundingCostRepository.save(FundingCost.builder()
                    .funding(funding)
                    .mainVariety(cost.getMainVariety())
                    .subVariety(cost.getSubVariety())
                    .build());
        }

        // -1 리턴 시, 저장이 되지 않은 것
        // 1 이상 리턴 시, 저장이 된 것

        // 저장 여부는 영속화 여부에 따라 확인한다.
        // 정상적으로 저장되었다면 영속화된 상태일 것!
        if (!em.contains(funding)) {
            return -1L;
        }
        return funding.getId();
    }

    @Override
    public FundingResponseDto getFunding(Long fundingId) {
        Funding funding = fundingRepository.findById(fundingId)
                .orElse(null);

        if (funding != null) {
            return new FundingResponseDto(funding);
        } else {
            return null;
        }
    }

    @Override
    @Transactional
    public List<FundingListItemDto> getFundingListByCategoryAndStatus(String category, String status, String allowStat, Pageable pageable) {
        String UPPER_ALL = "ALL";
        String RECRUITMENT_END = "RECRUITMENT_END"; // 모집 종료(모집 성공(정산 대기), 모집 실패)

        List<FundingProgressStatus> statuses;
        if (status.toUpperCase(Locale.ROOT).equals(UPPER_ALL) || (Stream.of(FundingProgressStatus.values())
                .noneMatch(ps -> ps.name().equals(status)) && !status.toUpperCase(Locale.ROOT).equals(RECRUITMENT_END))) { // 모집 시작 전을 제외한 모든 진행 상태
            statuses = List.of(FundingProgressStatus.RECRUITMENT_STATUS,
                    FundingProgressStatus.PENDING_SETTLEMENT, FundingProgressStatus.SETTLED,
                    FundingProgressStatus.RECRUITMENT_FAILED);
        } else if (status.toUpperCase(Locale.ROOT).equals(RECRUITMENT_END)) {
            statuses = List.of(FundingProgressStatus.PENDING_SETTLEMENT,
                    FundingProgressStatus.RECRUITMENT_FAILED);
        } else {
            statuses = List.of(FundingProgressStatus.valueOf(status));
        }

        List<Boolean> allowStatuses;
        if (allowStat.toUpperCase(Locale.ROOT).equals(UPPER_ALL) || Stream.of(FundingAllowStatus.values())
                .noneMatch(als -> als.name().equals(allowStat))) {
            allowStatuses = List.of(true, false);
        } else {
            allowStatuses = List.of(allowStat.toUpperCase(Locale.ROOT).equals("TRUE"));
        }

        Page<Funding> fundingPage;
        if (category.toUpperCase(Locale.ROOT).equals(UPPER_ALL)) {
            fundingPage = fundingRepository.findAllByProgressStatusInAndIsAllowIn(statuses, allowStatuses, pageable);
        } else {
            fundingPage = fundingRepository.findAllByCategoryAndProgressStatusInAndIsAllowIn(category, statuses, allowStatuses, pageable);
        }

        return fundingPage.getContent()
                .stream()
                .map(FundingListItemDto::new)
                .toList();
    }

    @Override
    @Transactional
    public int allowFunding(Long fundingId, String allowStatus, CustomUserDetails member) {
        Funding funding = fundingRepository.findById(fundingId)
                .orElse(null);
        if (member.getAuthorities().stream().noneMatch(au -> au.getAuthority().equals(ROLE_ADMIN))) {
            return -2;
        } else if (funding == null) {
            return -1;
        } else if (funding.getIsAllow() != null) {
            return 0;
        } else {
            funding.allowFunding(FundingAllowStatus.TRUE.name().equals(allowStatus.toUpperCase(Locale.ROOT)));
            return 1;
        }
    }

    @Override
    @Transactional
    public int updateFundingProgressStatus(Long fundingId, String progressStatus, CustomUserDetails member) {
        Funding funding = fundingRepository.findById(fundingId)
                .orElse(null);

        if (funding == null) {
            return -1;
        }

        if (member.getAuthorities().stream().noneMatch(au -> au.getAuthority().equals(ROLE_ADMIN)
                || (au.getAuthority().equals(ROLE_COMPANY) && member.getId().equals(funding.getEntId())))) {
            return -2;
        }

        if (Stream.of(FundingProgressStatus.values())
                .noneMatch(ps -> ps.name().equals(progressStatus))) {
            return 0;
        }

        funding.updateProgressStatus(FundingProgressStatus.valueOf(progressStatus));
        return 1;
    }

    @Override
    public List<FundingPermissionResponseDto> getFundingPermissionList() {
        List<FundingPermissionResponseDto> list = fundingRepository.findAllByIsAllowIsNullAndComName();
        return list;
    }

    @Override
    public int createNotice(Long fundingId, FundingNoticeRequestDto dto, CustomUserDetails member) {
        Funding funding = fundingRepository.findById(fundingId).orElse(null);
        if (funding == null) {
            return -1;
        }

        if (member.getAuthorities().stream().noneMatch(au -> (au.getAuthority().equals(ROLE_COMPANY) && member.getId().equals(funding.getEntId())))) {
            return -2;
        }

        FundingNotice fundingNotice = fundingNoticeRepository.save(
                FundingNotice.builder()
                        .funding(funding)
                        .title(dto.getTitle())
                        .content(dto.getContent())
                        .build()
        );

        // 저장 여부는 영속화 여부에 따라 확인한다.
        // 정상적으로 저장되었다면 영속화된 상태일 것!
        if (!em.contains(fundingNotice)) {
            return 0;
        }
        return 1;
    }

    @Override
    public FundingNoticeResponseDto getFundingNotice(Long fundingId, Long fundingNoticeId) {
        FundingNotice fundingNotice = fundingNoticeRepository.findById(fundingNoticeId)
                .orElse(null);

        if (fundingNotice == null || !Objects.equals(fundingNotice.getFunding().getId(),
                fundingId)) {
            return null;
        }

        return new FundingNoticeResponseDto(fundingNotice);
    }

    @Override
    @Transactional
    public int updateFundingNotice(Long fundingId, Long fundingNoticeId,
                                   FundingNoticeRequestDto dto, CustomUserDetails member) {
        FundingNotice fundingNotice = fundingNoticeRepository.findById(fundingNoticeId)
                .orElse(null);

        if ((fundingNotice == null) || !fundingNotice.getFunding().getId().equals(fundingId)) {
            return -1;
        }

        if (member.getAuthorities().stream().noneMatch(au -> (au.getAuthority().equals(ROLE_COMPANY) && member.getId().equals(fundingNotice.getFunding().getEntId())))) {
            return -2;
        }

        fundingNotice.updateTitleAndContent(dto.getTitle(), dto.getContent());
        return 1;
    }

    @Override
    public int deleteFundingNotice(Long fundingId, Long fundingNoticeId, CustomUserDetails member) {
        FundingNotice fundingNotice = fundingNoticeRepository.findById(fundingNoticeId)
                .orElse(null);

        if (fundingNotice == null || !Objects.equals(fundingNotice.getFunding().getId(),
                fundingId)) {
            return -1;
        }

        if (member.getAuthorities().stream().noneMatch(au -> (au.getAuthority().equals(ROLE_COMPANY) && member.getId().equals(fundingNotice.getFunding().getEntId())))) {
            return -2;
        }

        fundingNoticeRepository.delete(fundingNotice);
        return 1;
    }

    @Override
    @Transactional
    public Long createInvestmentLog(Long fundingId, InvestmentRequestDto dto, CustomUserDetails member) {
        Member memberInfo = memberRepository.findById(member.getId())
                .orElse(null);
        if (memberInfo == null || member.getAuthorities().stream().noneMatch(au -> au.getAuthority().equals(ROLE_USER))) {
            return -2L;
        }

        Funding funding = fundingRepository.findById(fundingId)
                .orElse(null);
        if (funding == null) {
            return -1L;
        }
        if (!funding.getProgressStatus().equals(FundingProgressStatus.RECRUITMENT_STATUS)) {
            return 0L;
        }

        funding.renewNowCoinCount(funding.getNowCoinCount() + dto.getCoinCount());

        InvestmentLog savedInvestmentLog = investmentLogRepository.save(
                InvestmentLog
                        .builder()
                        .member(memberInfo)
                        .funding(funding)
                        .transactionHash(dto.getTransactionHash())
                        .transactionTime(dto.getTransactionTime())
                        .coinCount(dto.getCoinCount())
                        .pieceCount(dto.getPieceCount())
                        .build()
        );
        return savedInvestmentLog.getId();
    }
}