package com.pw.quizwhizz.service.impl;

import com.pw.quizwhizz.model.dto.Ranking;
import com.pw.quizwhizz.model.dto.Statistics;
import com.pw.quizwhizz.repository.UserRepository;
import com.pw.quizwhizz.repository.impl.RankingRepository;
import com.pw.quizwhizz.repository.impl.StatisticsRepository;
import com.pw.quizwhizz.service.StatService;
import com.pw.quizwhizz.util.ImageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Serwis udostepniajacy dane statystyczne dla portalu (statystyki, rankingi)
 * @author Michał Nowiński
 * @see StatService
 */
@Service
public class StatServiceImpl implements StatService {
    final private RankingRepository rankingRepository;
    final private ImageUtil imageUtil;
    final private StatisticsRepository statisticsRepository;
    final private UserRepository userRepository;

    @Autowired
    public StatServiceImpl(RankingRepository rankingRepository, ImageUtil imageUtil, StatisticsRepository statisticsRepository, UserRepository userRepository) {
        this.rankingRepository = rankingRepository;
        this.imageUtil = imageUtil;
        this.statisticsRepository = statisticsRepository;
        this.userRepository = userRepository;
    }

    @Override
    public int findNumberOfUsers(){
        return userRepository.countAll();
    }

    @Override
    public Statistics findStatistic(){
        return statisticsRepository.findStatistic();
    }

    @Override
    public List<Ranking> findGeneralRank(int limitSearch){
        return rankingRepository.findGeneralRank(limitSearch);
    }

    @Override
    public List<Ranking> findFiveByCategory(int limitSearch , Long categoryId){
        return rankingRepository.findByCategory(limitSearch, categoryId);
    }
}