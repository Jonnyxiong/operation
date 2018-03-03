package com.ucpaas.sms.controller;

import com.jsmsframework.common.dto.R;
import com.jsmsframework.common.entity.JsmsNoticeList;
import com.jsmsframework.common.exception.JsmsNoticeListException;
import com.ucpaas.sms.common.util.DateUtils;
import com.ucpaas.sms.common.util.SecurityUtils;
import com.ucpaas.sms.common.util.StringUtils;
import com.ucpaas.sms.entity.UserSession;
import com.ucpaas.sms.entity.message.Notice;
import com.ucpaas.sms.enums.SessionEnum;
import com.ucpaas.sms.service.notice.NoticeService;
import com.ucpaas.sms.util.ConfigUtils;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * created by xiaoqingwen on 2017/12/5 10:20
 * 公告管理相关
 */
@Controller
@RequestMapping("/notice")
public class NoticeController {
    private static Logger logger = LoggerFactory.getLogger(NoticeController.class);
    @Autowired
    private NoticeService noticeService;

    /**
     * 公告管理
     * @param mv
     * @return
     */
    @ApiOperation(value = "跳转到公告管理页面", notes = "跳转到公告管理页面", tags = "公告管理",response = ModelAndView.class)
    @RequestMapping(path = "/noticeList", method = RequestMethod.GET)
    public ModelAndView showNoticeList(ModelAndView mv) {
        mv.setViewName("notice/noticeList");
        return mv;
    }

    /**
     * 公告管理之搜索(分页)按钮
     */
    //http://localhost:8080/notice/searchNotice?noticeNameOrRealname=云之讯&webId=4&status=0&currentPage=1&pageRowCount=20
    @ApiOperation(value = "搜索和翻页按钮", notes = "搜索和翻页按钮", tags = "公告管理",response = Map.class)
    @RequestMapping(path = "/searchNotice", method = RequestMethod.POST)
    @ResponseBody
    public Map<String,Object> searchNotice(String noticeNameOrRealname, Integer webId, Integer status,@RequestParam(value="page", defaultValue="1")Integer page,@RequestParam(value="rows", defaultValue="20")Integer rows){
        //return noticeService.showNoticeList("云之讯", webId, status, currentPage, pageRowCount);
        return noticeService.showNoticeList(noticeNameOrRealname, webId, status, page, rows);
    }

    /**
     * 公告删除
     */
    @ApiOperation(value = "公告删除", notes = "公告删除", tags = "公告管理",response = com.jsmsframework.common.dto.R.class)
    @ApiImplicitParams({@ApiImplicitParam(name = "id", value = "公告id", dataType = "int",required = true, paramType="query")
    })
    @RequestMapping(path = "/deleteNotice", method = RequestMethod.POST)
    @ResponseBody
    public R deleteNotice(int id) {
        R r = null;
        try {
            r = noticeService.deleteNotice(id);
        } catch (JsmsNoticeListException e) {
            logger.error("删除失败!",e);
            R.error(e.getMessage());
        }catch (Exception e){
            logger.error("删除公告失败!",e);
            R.error(e.getMessage());
        }
        return r;
    }
    /**
     * 公告状态改变按钮
     */
    @ApiOperation(value = "公告状态改变", notes = "公告状态改变", tags = "公告管理",response = com.jsmsframework.common.dto.R.class)
    @ApiImplicitParams({@ApiImplicitParam(name = "status", value = "公告状态", dataType = "int",required = true, paramType="query"),
            @ApiImplicitParam(name = "id", value = "公告id", dataType = "int",required = true, paramType="query")
    })
    @RequestMapping(path = "/updateStatus", method = RequestMethod.POST)
    @ResponseBody
    public R updateStatus(int status, int id) {
        R r=null;
        try {
            r = noticeService.updateStatus(status, id);
        } catch (JsmsNoticeListException e) {
            logger.error("状态改变失败!",e);
            return R.error(e.getMessage());
        } catch (Exception e) {
            logger.error("状态改变失败!",e);
            return R.error("状态改变失败!");
        }
        return r;
    }

    /**
     * 公告编辑
     */
    @ApiOperation(value = "公告编辑跳转页面", notes = "公告编辑跳转页面", tags = "公告管理",response =ModelAndView.class)
    @RequestMapping(path = "/edit", method = RequestMethod.GET)
    public ModelAndView noticeDetails(ModelAndView mv,int id) {
        Notice notice = new Notice();
        notice.setId(id);
        mv.setViewName("notice/addNotice");

        R r = noticeService.edit(notice);
        mv.addObject(r);
        return mv;
    }

    /**
     * 公告详情
     * @return
     */
    @ApiOperation(value = "公告详情", notes = "公告详情", tags = "公告管理",response =ModelAndView.class)
    @RequestMapping(path = "/noticeDetails", method = RequestMethod.GET)
    @ResponseBody
    public ModelAndView noticeDetails(int id,ModelAndView mv) {
        mv.setViewName("notice/noticeDetails");
        Notice notice = new Notice();
        notice.setId(id);
        R r = noticeService.noticeDetails(notice);
        mv.addObject(r);
        return mv;
    }

    /**
     * 添加公告
     * @param mv
     * @return
     */
   @RequestMapping(path = "/addNotice", method = RequestMethod.GET)
    public ModelAndView addNotice(ModelAndView mv) {
       mv.setViewName("notice/addNotice");
       //为了避免写两个页面传参不传值
       Map<String,Object> data=new HashMap<>();
       Notice noticeDetails = new Notice();
       data.put("noticeDetails",noticeDetails);

       // 放入上传路径
       String smspImgUrl = ConfigUtils.smsp_img_url.endsWith("/") ? ConfigUtils.smsp_img_url.substring(0, ConfigUtils.smsp_img_url.lastIndexOf("/")) : ConfigUtils.smsp_img_url;
       data.put("path", smspImgUrl+"/upload/uploadAutoNew.html");

       mv.addObject(R.ok("succes",data));
       return mv;
    }

    /**
     * 添加公告
     * @para
     * @return
     */
    @RequestMapping(path = "/addNotice", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "添加公告", notes = "添加公告", tags = "公告管理", response =com.jsmsframework.common.dto.R.class)
    public R addNotice(Integer id,String isTop,String noticeContent,String noticeName,String releaseTime,String webId, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute(SessionEnum.SESSION_USER.toString());
        JsmsNoticeList notice = new JsmsNoticeList();
        if(user==null){
            return R.error("用户未登入!");
        }
        notice.setAdminId(user.getId());
        notice.setId(id);
        notice.setIsTop(Integer.valueOf(isTop));
        notice.setNoticeContent(noticeContent);
        notice.setNoticeName(noticeName);
        if (StringUtils.isBlank(releaseTime)){
            notice.setReleaseTime(null);
        }else{
           notice.setReleaseTime(DateUtils.parseDate(releaseTime));
        }
        notice.setWebId(Integer.valueOf(webId));
        R r = null;
        try {
            r = noticeService.addNotice(notice);
        } catch (JsmsNoticeListException e){
            logger.error("发布公告失败!",e);
            r=R.error(e.getMessage());
        }catch (Exception e) {
            logger.error("发布公告失败!",e);
            r=R.error(e.getMessage());
        }
        return r;
    }
    /**
     * 公告列表
     * @param mv
     * @return
     */
    @ApiOperation(value = "公告列表", notes = "公告列表", tags = "公告管理",response = ModelAndView.class)
    @RequestMapping(path = "list", method = RequestMethod.GET)
    public ModelAndView list(ModelAndView mv) {
        mv.setViewName("notice/list");
        return mv;
    }

    /**
     * 公告列表(翻页)
     * @return
     */
    @ApiOperation(value = "公告列表(翻页)", notes = "公告列表(翻页)", tags = "公告管理",response = Map.class)
    @RequestMapping(path = "list", method = RequestMethod.POST)
    @ResponseBody
    public Map<String,Object> list(@RequestParam(value="page", defaultValue="1")Integer page,@RequestParam(value="rows", defaultValue="20")Integer rows) {
        return noticeService.list(page,rows);
    }

    @PostMapping("/uploadAuto")
    @ResponseBody
    public Map<String,Object> uploadAuto(HttpServletRequest request){
        String fileFormats = request.getParameter("fileFormats");
        String fileSize = request.getParameter("fileSize");
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        CommonsMultipartFile photoFile = (CommonsMultipartFile) multipartRequest.getFile("photoFile");
        Map<String,Object> data = new HashMap<String,Object>();
        // 判断文件是否为空
        if (photoFile != null && !photoFile.isEmpty()) {

            data = checkPhotoFile(photoFile,fileFormats,fileSize);
            // 如果图片的不符合规则,则返回
            if(data.get("result") != null && !"success".equals(data.get("result"))) return data;
            String fileName = UUID.randomUUID().toString() + data.get("suffix");
            // 获取保存文件的临时路径
            String sysPath = ConfigUtils.client_oauth_pic;
            // 容错判断路径是否以 "/"结尾
            if(sysPath.endsWith("/")) sysPath = sysPath.substring(0, sysPath.lastIndexOf("/"));
            // 在 路径中添加日期路径
            String path = new SimpleDateFormat("/yyyy/MM/dd/").format(new Date());
            logger.debug("保存图片的路径为----------------->{}", sysPath + path);
            File saveFile = new File(sysPath + path + fileName);
            // 判断上传文件的所在的文件夹是否存在
            if (!saveFile.getParentFile().exists()) {
                boolean mkdir = saveFile.getParentFile().mkdirs();
                logger.debug("文件的所在文件夹不存在, 创建 成功 ?----------------->{}", mkdir);
            }

            String ctx = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
                    + request.getContextPath();

            data = toSaveFile(photoFile, saveFile, ctx);
            return data;
        }
        data.put("result", "none");
        data.put("msg","没有上传文件");
        return data;
    }

    @ApiOperation(value = "获取参数信息-图片信息", notes = "系统参数-获取参数信息-图片信息", tags = "系统管理-系统参数", response = R.class)
    @GetMapping("/img")
    @ResponseBody
    public void imgDown(String path, HttpServletResponse response) {
        if (StringUtils.isNotBlank(path)) {
            download(SecurityUtils.decodeDes3(path), response);
        }
    }

    public void download(String path, HttpServletResponse response) {
        // String fileName = path.substring(path.lastIndexOf("/") + 1);
        InputStream in = null;
        BufferedOutputStream out = null;

        try {
            in = new FileInputStream(path);
            response.reset();

            response.setHeader("Accept-Ranges", "bytes");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Cache-Control", "no-cache");
            response.setDateHeader("Expires", 0);

            out = new BufferedOutputStream(response.getOutputStream());
            byte[] buffer = new byte[16384];

            int len;
            while ((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }

            out.flush();
            logger.debug("下载文件【成功】：path=" + path);
        } catch (FileNotFoundException var17) {
            logger.debug("下载文件【文件不存在】：path=" + path);
        } catch (Throwable var18) {
            logger.error("下载文件【失败】：path=" + path, var18);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }

                if (out != null) {
                    out.close();
                }
            } catch (IOException var16) {
                logger.error("关闭文件【失败】：path=" + path, var16);
            }
        }
    }

    /**
     * @Description: 根据saveFile中信息,保存photoFile文件
     * @author: Niu.T
     * @date: 2016年11月11日    下午4:49:19
     */
    private Map<String, Object> toSaveFile(MultipartFile file,File saveFile, String ctx) {
        Map<String,Object> data = new HashMap<String,Object>();
        try {
            file.transferTo(saveFile);		// 保存临时图片

            String path = saveFile.getAbsolutePath();
            if(path !=null) {
                String smspImgUrl = ConfigUtils.smsp_img_url.endsWith("/") ? ConfigUtils.smsp_img_url.substring(0, ConfigUtils.smsp_img_url.lastIndexOf("/")) : ConfigUtils.smsp_img_url;
                data.put("path", smspImgUrl+"/file/scanPic.html?path="+SecurityUtils.encodeDes3(path));

                //data.put("path", ctx+"/notice/img?path="+ SecurityUtils.encodeDes3(path));
            }

            //data.put("path",saveFile.getAbsolutePath());				// 封装临时文件所在的全路径信息
            data.put("result","success");
            data.put("msg","上传成功");
            logger.debug("上传文件成功 ------------->{}",data);
        } catch (IllegalStateException | IOException e) {
            logger.debug("上传文件失败 ------------->{}",e.getMessage());
            data.put("result","fail");
            data.put("msg","上传失败");
        }
        return data;
    }

    private  Map<String, Object>  checkPhotoFile(MultipartFile photoFile, String fileFormats, String fileSize) {
        Map<String, Object> data = new HashMap<String,Object>();
        String suffix = photoFile.getOriginalFilename().substring(photoFile.getOriginalFilename().lastIndexOf("."));

        if(StringUtils.isNotEmpty(fileFormats) && !checkFormats(fileFormats.toLowerCase(),suffix)){
            data.put("msg", "对不起，上传的图片格式必须是"+fileFormats+"格式！");
            data.put("result", "fail");
            logger.debug("图片格式不是{}！当前格式------------->{}",fileFormats,suffix );
            return data;
        }else if (!suffix.toLowerCase().equals(".gif") && !suffix.toLowerCase().equals(".jpg")
                && !suffix.toLowerCase().equals(".png")&& !suffix.toLowerCase().equals(".jpeg")) {								// 判断文件类型
            data.put("msg", "对不起，上传的图片格式必须是jpg,gif,jpeg格式！");
            data.put("result", "fail");
            logger.debug("图片格式不是gif、jpg、jpeg！当前格式------------->{}",suffix );
            return data;
        }

        long size = photoFile.getSize();

        if (fileSize != null && isNumeric(fileSize) && Integer.valueOf(fileSize) < size ) {			// 判断文件大小
            data.put("msg", "对不起，上传的图片大小不能超过"+ Integer.valueOf(fileSize)/1024+ "MB！");
            data.put("result", "fail");
            logger.debug("图片大小不能超过{}M, 当前大小 ------------->{}",Integer.valueOf(fileSize),size);
            return data;
        }else if(size > 1024 * 5120){
            data.put("msg", "对不起，上传的图片大小不能超过5MB！");
            data.put("result", "fail");
            logger.debug("图片大小不能超过5M, 当前大小 ------------->{}",size);
            return data;
        }
        data.put("result", "success");
        data.put("suffix", suffix);	// 返回图片的后缀
        return data;
    }

    private boolean checkFormats(String fileFormats,String suffix){
        String[] split = fileFormats.split(",");
        for (String s:split) {
            if(!s.startsWith("."))
                s = "." + s;
            if(suffix.toLowerCase().equals(s))
                return true;
        }
        return false;
    }

    /**
     * 判断是否是数字字符串
     * @param str
     * @return
     */
    private boolean isNumeric(String str){
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if( !isNum.matches() ){
            return false;
        }
        return true;
    }
    //自己测试自己远程仓库5.17.2
}
