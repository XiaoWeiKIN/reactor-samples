package wangxw.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @Author: wangxw
 * @Date: 2021/09/07
 * @Description:
 */
@Data
@AllArgsConstructor
public class Ticket {
    private Flight flight;
    private Passenger passenger;
}
