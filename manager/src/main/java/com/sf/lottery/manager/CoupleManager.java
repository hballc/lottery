package com.sf.lottery.manager;

import com.sf.lottery.common.utils.RandomUtil;
import com.sf.lottery.common.vo.CpGiftVo;
import com.sf.lottery.dao.CoupleMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by 01139954 on 2016/12/11.
 */
@Component
public class CoupleManager {
    private final static Logger log = LoggerFactory.getLogger(CoupleManager.class);

    @Autowired
    private CoupleMapper coupleMapper;

    public List<CpGiftVo> getAllCouple(){
        return coupleMapper.selectAllCouple();
    }

    public List<CpGiftVo> getAllUnAwardCouple(){
        return coupleMapper.selectUnAwardCouple();
    }


    public CpGiftVo getLuckCP(){
        CpGiftVo luckCP = new CpGiftVo();
        List<CpGiftVo> unAwardCp = coupleMapper.selectUnAwardCouple();
        if(unAwardCp.size() > 0){
            //[0,unAwardCp.size())  前闭后开区间
            int luckIndex = (int) (Math.random() * (unAwardCp.size()));
            luckCP = unAwardCp.get(luckIndex);
            coupleMapper.updateAwardStatusById(luckCP.getId());
        }else {
            log.info("没有未获奖的CP");
        }
        return luckCP;
    }

}