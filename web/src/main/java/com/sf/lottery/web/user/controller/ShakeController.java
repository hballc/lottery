package com.sf.lottery.web.user.controller;

import com.alibaba.fastjson.JSON;
import com.sf.lottery.common.dto.JsonResult;
import com.sf.lottery.common.model.User;
import com.sf.lottery.service.ConfigService;
import com.sf.lottery.service.UserService;
import com.sf.lottery.web.gift.UserShakeVo;
import com.sf.lottery.web.sort.NonThreadSafeDescSortedMostNList;
import com.sf.lottery.web.utils.CookiesUtil;
import com.sf.lottery.web.websocket.WebsocketClientFactory;
import org.java_websocket.client.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wujiang
 * @version 1.0.0.
 * @date 2016/12/21
 */
@Controller
public class ShakeController {
    private final static Logger log = LoggerFactory.getLogger(com.sf.lottery.web.user.controller.ShakeController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private ConfigService configService;
    //
    //@Value("${shake.websocket.address}")
    //private String shakeAddress;

    //singleton
    private Map<Integer,UserShakeVo> shakeCountMap = new ConcurrentHashMap<>();

    private UserShakeVo shakeWinner = new UserShakeVo();


    @ResponseBody
    @RequestMapping(value = "/shake/saveShakeCount", method = RequestMethod.POST)
    public JsonResult<String> saveShakeCount(@RequestParam("shakeCount") int shakeCount, HttpServletRequest request){
        JsonResult<String> result = new JsonResult<>();
        Cookie cookie = CookiesUtil.getCookieByName(request, "userId");
        if(cookie == null){
            log.info("用户未登陆");
            result.setData("亲！您还未登陆");
            return result;
        }
        try {
            String userIdStr = cookie.getValue();
            int userId = Integer.parseInt(userIdStr);
            if(shakeCountMap.containsKey(userId)){
                UserShakeVo curUserShakeVo = shakeCountMap.get(userId);
                curUserShakeVo.setShakeCount(curUserShakeVo.getShakeCount()+shakeCount);
                shakeCountMap.put(userId, curUserShakeVo);//存的是引用，不用写这一句，只是为了阅读方便
            }else {
                User user = userService.getUserById(userId);
                //只有未获奖的人才可以进行摇一摇抽奖
                if(user.getAwardCount() == 0){
                    UserShakeVo userShakeVo = new UserShakeVo();
                    userShakeVo.setUserId(user.getId());
                    userShakeVo.setUserName(user.getSfName());
                    userShakeVo.setHeadImgUrl(user.getWxHeadimgurl());
                    userShakeVo.setUserNo(user.getSfNum());//工号
                    userShakeVo.setShakeCount(shakeCount);//摇的次数
                    shakeCountMap.put(userId,userShakeVo);//重新放一个相同的key,会自动覆盖value
                }
                result.setData("true");
            }
        } catch (Exception e) {
            log.info("该用户不存在");
            e.printStackTrace();
            result.setData("该用户不存在");
        }
        return result;
    }

    @ResponseBody
    @RequestMapping(value = "/test/saveShakeCount", method = RequestMethod.GET)
    public JsonResult<String> savShakeCount(@RequestParam("shakeCount") int shakeCount,@RequestParam("userId") int userId, HttpServletRequest request){
        JsonResult<String> result = new JsonResult<>();
        try {
            if(shakeCountMap.containsKey(userId)){
                UserShakeVo curUserShakeVo = shakeCountMap.get(userId);
                curUserShakeVo.setShakeCount(curUserShakeVo.getShakeCount()+shakeCount);
                shakeCountMap.put(userId, curUserShakeVo);//存的是引用，不用写这一句，只是为了阅读方便
            }else {
                User user = userService.getUserById(userId);
                //只有未获奖的人才可以进行摇一摇抽奖
                if(user.getAwardCount() == 0){
                    UserShakeVo userShakeVo = new UserShakeVo();
                    userShakeVo.setUserId(user.getId());
                    userShakeVo.setUserName(user.getSfName());
                    userShakeVo.setHeadImgUrl(user.getWxHeadimgurl());
                    userShakeVo.setUserNo(user.getSfNum());//工号
                    userShakeVo.setShakeCount(shakeCount);//摇的次数
                    shakeCountMap.put(userId,userShakeVo);//重新放一个相同的key,会自动覆盖value
                }
                result.setData("true");
            }
        } catch (Exception e) {
            log.info("该用户不存在");
            e.printStackTrace();
            result.setData("该用户不存在");
        }
        return result;
    }

    @ResponseBody
    @RequestMapping(value = "/shake/getTopN", method = RequestMethod.GET)
    public JsonResult<LinkedList<Comparable>> getTopN(@RequestParam("topSize") int topSize){
        JsonResult<LinkedList<Comparable>> result = new JsonResult<>();
        NonThreadSafeDescSortedMostNList sortList = new NonThreadSafeDescSortedMostNList(topSize);
        Map<Integer, UserShakeVo> temp = new HashMap<>();
        temp.putAll(shakeCountMap);
        Iterator<Map.Entry<Integer, UserShakeVo>> entries = temp.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<Integer, UserShakeVo> entry = entries.next();
            sortList.put(entry.getValue());
        }
        result.setData(sortList.getResult());
        return result;
    }

    @ResponseBody
    @RequestMapping(value = "/shake/isCanShake", method = RequestMethod.POST)
    public JsonResult<Boolean> isStartShake(){
        JsonResult<Boolean> result = new JsonResult<>();
        result.setData(configService.isCanShake());
        return result;
    }

    @ResponseBody
    @RequestMapping(value = "/shake/openShake", method = RequestMethod.POST)
    public JsonResult<Boolean> startShake(){
        JsonResult<Boolean> result = new JsonResult<>();
        try {
            configService.setCurStateShake(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        shakeCountMap = new ConcurrentHashMap<>();
        result.setData(configService.openShake() > 0);
        return result;
    }

    @ResponseBody
    @RequestMapping(value = "/shake/closeShake", method = RequestMethod.POST)
    public JsonResult<UserShakeVo> closeShake(){
        configService.closeShake();
        JsonResult<UserShakeVo> result = new JsonResult<>();
        NonThreadSafeDescSortedMostNList sortList = new NonThreadSafeDescSortedMostNList(1);
        Map<Integer, UserShakeVo> temp = new HashMap<>();
        temp.putAll(shakeCountMap);
        Iterator<Map.Entry<Integer, UserShakeVo>> entries = temp.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<Integer, UserShakeVo> entry = entries.next();
            sortList.put(entry.getValue());
        }
        LinkedList<Comparable> topList = sortList.getResult();
        Comparable topUser = topList.getFirst();
        if(topUser != null){
            UserShakeVo luckUser = (UserShakeVo)topUser;
            result.setData(luckUser);
            try {
                //将获奖人持久化到数据库
                userService.setUserShakeAward(luckUser.getUserId());
                shakeWinner = luckUser;
                configService.setCurStateShake(2);
                //发送到前台显示
                //WebSocketClient webSocketClient = WebsocketClientFactory.getWebsocketClient("shake", shakeAddress);
                //webSocketClient.connectBlocking();
                //webSocketClient.send(JSON.toJSONString(luckUser));
                //webSocketClient.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        shakeCountMap = new ConcurrentHashMap<>();
        return result;
    }

    @ResponseBody
    @RequestMapping(value = "/shake/getShakeWinner", method = RequestMethod.POST)
    public JsonResult<UserShakeVo> getShakeWinner(){
        JsonResult<UserShakeVo> result = new JsonResult<>();
        try {
            result.setData(shakeWinner);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}

