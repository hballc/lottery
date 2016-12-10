package com.sf.lottery.serviceImpl;

import com.sf.lottery.manager.ConfigManager;
import com.sf.lottery.service.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by 01139954 on 2016/12/8.
 */
@Service
public class ConfigServiceImpl implements ConfigService {

    @Autowired
    private ConfigManager configManager;

    @Override
    public boolean setCurrentAward(int awardId) {
        return configManager.setCurrentAward(awardId);
    }

    @Override
    public boolean setCurrentOpera(int operaId) {
        return configManager.setCurrentOpera(operaId);
    }

    @Override
    public Boolean isCanShark() {
        return configManager.isCanShark();
    }

    @Override
    public int closeShark() {
        return configManager.closeShark();
    }

    @Override
    public int openShark() {
        return configManager.openShark();
    }

    @Override
    public Boolean isCanReward() {
        return configManager.isCanReward();
    }

    @Override
    public int closeReward() {
        return configManager.closeReward();
    }

    @Override
    public int openReward() {
        return configManager.openReward();
    }

    @Override
    public Boolean IsCanCpsign() {
        return configManager.IsCanCpsign();
    }

    @Override
    public int closeCpsign() {
        return configManager.closeCpsign();
    }

    @Override
    public int openCpsign() {
        return configManager.openCpsign();
    }
}