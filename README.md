## 基本信息

1. 着眼于统计英文词频；
2. 界面设计鲉 JavaFX 完成，因此运行依赖 JavaFX 运行时环境；
3. JavaFX 版本 17；
4. 判断文件编码用到了 org.mozilla.universalchardet.UniversalDetector ，库文件在 "./lib" 目录下，非常棒的工具。 Maven 中央仓库[地址](https://central.sonatype.com/artifact/com.googlecode.juniversalchardet/juniversalchardet/1.0.3)

    配置：

    ```
    <dependency>
        <groupId>com.googlecode.juniversalchardet</groupId>
        <artifactId>juniversalchardet</artifactId>
        <version>1.0.3</version>
    </dependency>
    ```

## JavaFX 配置

这个项目实现了在 VS Code 默认的 Java 项目结构下直接 f5 调适 JavaFX 程序。

在项目的 settings.json 中配置 "java.configuration.runtimes" 为使用 jlink 工具生成的包含 JavaFX 模块的 jre.

这样就无需将 JavaFX 相关的 .jar 文件放在 lib 文件夹下（第三方库除外）就可以顺利在 VS Code 中调适和编写 JavaFX 程序了。
