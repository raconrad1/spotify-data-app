package spotify;

import spotify.model.TopStatsCollector;
import spotify.model.GeneralStatsCollector;
import spotify.model.DailyStatsCollector;
import spotify.model.YearlyStatsCollector;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class StatsAggregator {
    public static CombinedStatsCollector cachedStats = null;

    public CombinedStatsCollector getStats() { return cachedStats; }

    public static class CombinedStatsCollector {
        public TopStatsCollector topStats;
        public GeneralStatsCollector generalStats;
        DailyStatsCollector dailyStats;
        YearlyStatsCollector yearlyStats;

        public CombinedStatsCollector(TopStatsCollector topStats, GeneralStatsCollector generalStats, DailyStatsCollector dailyStats, YearlyStatsCollector yearlyStats) {
            this.topStats = topStats;
            this.generalStats = generalStats;
            this.dailyStats = dailyStats;
            this.yearlyStats = yearlyStats;
        }

        public TopStatsCollector getTopStats() { return topStats; }
        public GeneralStatsCollector getGeneralStats() { return generalStats; }
        public DailyStatsCollector getDailyStats() { return dailyStats; }
        public YearlyStatsCollector getYearlyStats() { return yearlyStats; }

    }

    public static CombinedStatsCollector computeStats(String folderPath) {
        TopStatsCollector topStats = new TopStatsCollector();
        GeneralStatsCollector generalStats = new GeneralStatsCollector();
        DailyStatsCollector dailyStats = new DailyStatsCollector();
        YearlyStatsCollector yearlyStats = new YearlyStatsCollector();

        DataService.processSessionFolder(folderPath, List.of(
                topStats::processEntry,
                generalStats::processEntry,
                dailyStats::processEntry,
                yearlyStats::processEntry
        ));
        generalStats.finalizeStats();
        dailyStats.finalizeStats();

        return new CombinedStatsCollector(topStats, generalStats, dailyStats, yearlyStats);
    }
}
