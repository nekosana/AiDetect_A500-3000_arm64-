import com.shark.aio.alarm.contactPart.util.MD5Util;
import com.shark.aio.alarm.contactPart.util.SendSms;
import com.shark.aio.alarm.entity.AlarmSettingsEntity;
import com.shark.aio.alarm.contactPart.util.ObjectUtil;
import com.shark.aio.user.captcha.SmsComponent;
import org.junit.jupiter.api.Test;

public class testUtil {
    @Test
    public void testIsEmpty(){
        AlarmSettingsEntity alarmSettingsEntity = new AlarmSettingsEntity();
        System.out.println(ObjectUtil.isEmpty(alarmSettingsEntity));
    }

    @Test
    public void testOS(){
        System.out.println(System.getProperty("os.name"));
    }


    @Test
    public void testSms(){
        String name = "tantianyi";
        String password = "Tty9004644";
        String MD5password = MD5Util.MD5(name + password);
        System.out.println(MD5password);

    }


}
