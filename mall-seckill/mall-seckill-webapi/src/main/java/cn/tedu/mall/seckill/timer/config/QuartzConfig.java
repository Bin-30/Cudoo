package cn.tedu.mall.seckill.timer.config;

import cn.tedu.mall.seckill.timer.job.SeckillInitialJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfig {

    @Bean
    public JobDetail initSeckill(){
        return JobBuilder.newJob(SeckillInitialJob.class)
                .withIdentity("initialSeckill")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger seckillTrigger(){
        CronScheduleBuilder cron = CronScheduleBuilder.cronSchedule("0 0/5 * * * ?");
        return TriggerBuilder.newTrigger()
                .forJob(initSeckill())
                .withIdentity("initialTrigger")
                .withSchedule(cron)
                .build();
    }

//    @Bean
//    public JobDetail bloomJob(){
//        return JobBuilder.newJob(SeckillBloomInitialJob.class)
//                .storeDurably()
//                .withIdentity("bloom")
//                .build();
//    }
//
//    @Bean
//    public Trigger bloomTrigger(){
//        return TriggerBuilder.newTrigger()
//                .forJob(bloomJob())
//                .withIdentity("bloomT")
//                .withSchedule(CronScheduleBuilder.cronSchedule("0 0/1 * * * ?"))
//                .build();
//    }

}
