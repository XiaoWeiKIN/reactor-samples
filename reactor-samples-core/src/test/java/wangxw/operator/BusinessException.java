package wangxw.operator;

/**
 * @Author: wangxw
 * @Date: 2021/09/06
 * @Description:
 */
public class BusinessException extends RuntimeException{
    public BusinessException() {
        super();
    }
    public BusinessException(String message) {
        super(message);
    }

}
