package com.example.mall.product.controller;

import com.example.mall.common.result.Result;
import com.example.mall.product.util.FileUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传的控制层
 */
@RestController
@RequestMapping("/admin/product")
public class FileController {
    @Value("${fileServer.url}")
    private String url;

    /**
     * 文件上传
     * @param file
     * @return
     */
    @PostMapping("/fileUpload")
    public Result fileUpload(@RequestParam MultipartFile file) throws Exception{
//        // 写法1  不建议这么写
//        // 读取配置文件的信息
//        ClassPathResource resource = new ClassPathResource("tracker.conf");
//        // 进行fastDFS的初始化
//        ClientGlobal.init(resource.getPath());
//        // 初始化tracker的连接
//        TrackerClient trackerClient = new TrackerClient();
//        // 通过客户端获取服务端的实例
//        TrackerServer trackerServer = trackerClient.getConnection();
//        // 通过tracker获取storage的信息
//        StorageClient storageClient = new StorageClient(trackerServer, null);
//        // 通过storage进行文件上传
//        /**
//         * 1. 文件字节大小
//         * 2. 文件的扩展名
//         * 3. 附加参数
//         */
//        String[] strings = storageClient.upload_file(file.getBytes(),
//                StringUtils.getFilenameExtension(file.getOriginalFilename()), null);
//        // 返回文件的地址：[0]=组名，[1]=全量路径名
//        return Result.ok(strings[0] + "/" + strings[1]);

        // 写法2：建议这么写
        String path = FileUtil.fileUpload(file);
        // 判断成功还是失败
        if (StringUtils.isEmpty(path)) {
            return Result.fail();
        }
        return Result.ok(url + path);
    }
}
