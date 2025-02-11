package com.shark.aio.base.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@Slf4j
public class Skip {

	@RequestMapping("/wastedWater")
	public String wastedWaterWeb() {
		return "wastedWater";
	}
	
	@RequestMapping("/carId")
	public String carIdWeb() {
		return "carId";
	}
	
	@RequestMapping("/wastedGas")
	public String wastedGasWeb() {
		return "wastedGas";
	}
	

	
	@RequestMapping("/face")
	public String faceWeb() {
		return "face";
	}
	
	@RequestMapping("/eleMonitor")
	public String eleMonitorWeb() {
		return "eleMonitor";
	}


	
	@RequestMapping("/alarm")
	public String alarmWeb() {
		return "alarm";
	}
	
	@RequestMapping("/map")
	public String mapWeb() {
		return "map";
	}
	

	
	@RequestMapping("/upload")
	public String uploadWeb() {
		return "upload";
	}
	@RequestMapping("/trace")
	public String traceWeb() {
		return "trace";
	}
	
	@RequestMapping("/file")
	public String fileWeb() {
		return "file";
	}
	
	@RequestMapping("/disk")
	public String diskWeb() {
		return "disk";
	}
	
	@RequestMapping("/user")
	public String userWeb() {
		return "user";
	}
	
	@RequestMapping("/authority")
	public String authorityWeb() {
		return "authority";
	}
	
	@RequestMapping("/log")
	public String logWeb() {
		return "log";
	}
	


	@RequestMapping("/heatMap")
	public String heatMapWeb() {
		return "heatMap";
	}

	@RequestMapping("/huiliu")
	public String huiliuWeb() {
		return "huiliu";
	}

	@RequestMapping("/traceHeatMap")
	public String traceHeatMapWeb() {
		return "traceHeatMap";
	}
	

	
	@RequestMapping("/alarmSettings")
	public String alarmSettingsWeb() {

		log.info("进入预警设置页面成功！");
		return "alarmSettings";
	}


}
