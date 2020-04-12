package com.sea.controller;

import com.sea.Util.QRCodeUtil;
import com.sea.Util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

@Controller
public class QrCodeController {

    @Autowired
    private RedisUtil redisUtil;

    private String baseImgUrl = "http://baidu.com/updateTokenState?token=";

    @RequestMapping("/generateQrCode")
    public String generateQrCode() throws Exception {
        //TODO token用时间戳和随机码的MD5
        String token = UUID.randomUUID().toString().replace("-", "");
        //设置为0表示没有被扫过
        redisUtil.setString(token, "0");
        //生成二维码，以及存放路径
        //二维码背景图
        String imgPath = "D:/code/img/sea.png";
        //二维码名称 token+.png
        String destPath = "D:/code/img/" + token + ".png";
        //生成二维码
        QRCodeUtil.encode(baseImgUrl + token, imgPath, destPath, true);
        // 解析二维码
        String str = QRCodeUtil.decode(destPath);
        // 打印出解析出的内容
        return token;
    }

    /**根据token获取二维码
     *
     * @param token
     * @return
     * @throws IOException
     */
    @RequestMapping("/generateQrCodeImg")
    public byte[] getQrCodeImg(@RequestParam String token) throws IOException {
        File file = new File("D:/code/img/" + token + ".png");
        FileInputStream inputStream = new FileInputStream(file);
        byte[] bytes = new byte[inputStream.available()];
        inputStream.read(bytes,0,inputStream.available());
        return  bytes;
    }

    /**
     * 用户扫码 将该token 的状态改为1
     *
     * @param token
     * @return
     */
    @RequestMapping("/updateTokenState")
    @ResponseBody
    public String updateTokenState(String token) {
        if (StringUtils.isEmpty(token)) {
            return "token 不能为空!";
        }
        // 将该token的状态改为1
        redisUtil.setString(token, "1");
        return "用户扫码成功";
    }

    /**
     * 前端使用定时器检查token状态
     *
     * @param token
     * @return
     */
    @RequestMapping("/checkToken")
    @ResponseBody
    public Boolean checkToken(String token) {
        if (StringUtils.isEmpty(token)) {
            return false;
        }
        String redisValue = redisUtil.getString(token);
        if (StringUtils.isEmpty(redisValue)) {
            return false;
        }
        if (!redisValue.equals("1")) {
            return false;
        }
        return true;
    }

    @RequestMapping("/sweepCode")
    @ResponseBody
    public String sweepCode() {
        return "恭喜您，扫码登陆成功!";
    }

}
