package com.atguigu.gmall.product.util;

import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 文件操作相关的工具类
 */
public class FileUtil {
    /**
     * 在静态模块中初始化fastDFS的配置
     */
    static {
        try {
            // 读取配置文件的信息
            ClassPathResource resource = new ClassPathResource("tracker.conf");
            // 进行fastDFS的初始化
            ClientGlobal.init(resource.getPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 文件上传
     * @param file
     * @return
     */
    public static String fileUpload(MultipartFile file) {
        try {
            // 初始化tracker的连接
            TrackerClient trackerClient = new TrackerClient();
            // 通过客户端获取服务端的实例
            TrackerServer trackerServer = trackerClient.getConnection();
            // 通过tracker获取storage的信息
            StorageClient storageClient = new StorageClient(trackerServer, null);
            // 通过storage进行文件上传
            /**
             * 1. 文件内容
             * 2. 文件的扩展名
             * 3. 附加参数
             */
            String[] strings = storageClient.upload_file(file.getBytes(),
                    StringUtils.getFilenameExtension(file.getOriginalFilename()), null);
            // 返回文件的地址：[0]=组名，[1]=全量路径名
            return strings[0] + "/" + strings[1];
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
