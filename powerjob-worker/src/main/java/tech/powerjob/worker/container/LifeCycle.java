package tech.powerjob.worker.container;

/**
 * life cycle
 *
 * @author tjq
 * @since 2020/5/15
 */
public interface LifeCycle {

    /**
     * initialization
     * @throws Exception initialization exception
     */
    void init() throws Exception;

    /**
     * destroy
     * @throws Exception destruction exception
     */
    void destroy() throws Exception;
}
