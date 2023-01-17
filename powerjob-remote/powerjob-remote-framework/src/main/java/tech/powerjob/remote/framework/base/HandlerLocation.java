package tech.powerjob.remote.framework.base;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * handler location
 *
 * @author tjq
 * @since 2022/12/31
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
public class HandlerLocation implements Serializable {
    /**
     * 根路径
     */
    private String rootPath;
    /**
     * 方法路径
     */
    private String methodPath;
    /**
     * 是否在本集群内（用于兼容 AKKA 等除了IP还需要指定 system 访问的情况）
     */
    private boolean insideCluster;

    public String toPath() {
        return String.format("/%s/%s", rootPath, methodPath);
    }
}