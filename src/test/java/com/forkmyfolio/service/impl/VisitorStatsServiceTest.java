package com.forkmyfolio.service.impl;

import com.forkmyfolio.dto.response.AdminStatsDto;
import com.forkmyfolio.model.VisitorStats;
import com.forkmyfolio.model.enums.VisitorStatType;
import com.forkmyfolio.repository.VisitorStatsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VisitorStatsServiceTest {

    @Mock
    private VisitorStatsRepository visitorStatsRepository;

    @InjectMocks
    private VisitorStatsService visitorStatsService;

    @Test
    void getStats_shouldCorrectlyAggregateStats() {
        // Arrange
        VisitorStats totalVisits = new VisitorStats();
        totalVisits.setType(VisitorStatType.TOTAL_VISITS);
        totalVisits.setRefId("site_total");
        totalVisits.setCount(100L);

        VisitorStats projectViews1 = new VisitorStats();
        projectViews1.setType(VisitorStatType.PROJECT_VIEW);
        projectViews1.setRefId("proj1");
        projectViews1.setCount(10L);

        VisitorStats projectViews2 = new VisitorStats();
        projectViews2.setType(VisitorStatType.PROJECT_VIEW);
        projectViews2.setRefId("proj2");
        projectViews2.setCount(20L);

        VisitorStats pdfDownloads = new VisitorStats();
        pdfDownloads.setType(VisitorStatType.PDF_DOWNLOAD);
        pdfDownloads.setRefId("pdf_download");
        pdfDownloads.setCount(5L);

        List<VisitorStats> allStats = Arrays.asList(totalVisits, projectViews1, projectViews2, pdfDownloads);
        when(visitorStatsRepository.findAll()).thenReturn(allStats);

        // Act
        AdminStatsDto statsDto = visitorStatsService.getStats();

        // Assert
        assertEquals(100L, statsDto.getTotalVisits());
        assertEquals(5L, statsDto.getPdfDownloads());
        assertEquals(2, statsDto.getProjects().size());
        assertEquals(10L, statsDto.getProjects().get("proj1"));
        assertEquals(20L, statsDto.getProjects().get("proj2"));
        verify(visitorStatsRepository, times(1)).findAll();
    }

    @Test
    void incrementStat_whenStatExists_shouldIncrementCount() {
        // Arrange
        VisitorStats existingStat = new VisitorStats();
        existingStat.setType(VisitorStatType.TOTAL_VISITS);
        existingStat.setRefId("site_total");
        existingStat.setCount(50L);

        when(visitorStatsRepository.findByTypeAndRefId(any(VisitorStatType.class), anyString()))
                .thenReturn(Optional.of(existingStat));

        ArgumentCaptor<VisitorStats> statsCaptor = ArgumentCaptor.forClass(VisitorStats.class);

        // Act
        visitorStatsService.incrementTotalVisits();

        // Assert
        verify(visitorStatsRepository, times(1)).save(statsCaptor.capture());
        VisitorStats savedStats = statsCaptor.getValue();
        assertEquals(51L, savedStats.getCount());
    }

    @Test
    void incrementStat_whenStatDoesNotExist_shouldCreateAndSetCountToOne() {
        // Arrange
        when(visitorStatsRepository.findByTypeAndRefId(any(VisitorStatType.class), anyString()))
                .thenReturn(Optional.empty());

        ArgumentCaptor<VisitorStats> statsCaptor = ArgumentCaptor.forClass(VisitorStats.class);

        // Act
        visitorStatsService.incrementTotalVisits();

        // Assert
        verify(visitorStatsRepository, times(1)).save(statsCaptor.capture());
        VisitorStats savedStats = statsCaptor.getValue();
        assertEquals(1L, savedStats.getCount());
        assertEquals(VisitorStatType.TOTAL_VISITS, savedStats.getType());
    }

    @Test
    void getStats_whenNoStatsExist_shouldReturnEmptyDto() {
        // Arrange
        when(visitorStatsRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        AdminStatsDto statsDto = visitorStatsService.getStats();

        // Assert
        assertEquals(0L, statsDto.getTotalVisits());
        assertEquals(0L, statsDto.getPdfDownloads());
        assertEquals(0, statsDto.getProjects().size());
    }
}
