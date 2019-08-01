package cn.ivhik.wyb.intellij.plugin;

/**
 * 全局配置接口
 *
 * @author maomao
 * @since 2019-04-05
 */
public interface GlobalConfig {

    /**
     * 插件标识
     *
     * <p>与 {@code /META-INF/plugin.xml} 中 {@code <id>} 内容一致</p>
     */
    String PLUGIN_ID = "cn.ivhik.wyb.intellij.plugin";

    /**
     * 插件名称
     */
    String PLUGIN_NAME = "一博鼓励师";

    /**
     * 插件打包后的 jar 包名称
     */
    String JAR_NAME = "wyb-intellij-plugin.jar";
}
