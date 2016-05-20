package com.daihan.test.captcha.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.captcha.Captcha;
import nl.captcha.audio.AudioCaptcha;
import nl.captcha.audio.producer.CustomNumberVoiceProducer;
import nl.captcha.backgrounds.GradiatedBackgroundProducer;
import nl.captcha.servlet.CaptchaServletUtil;
import nl.captcha.text.producer.CustomTextProducer;
import nl.captcha.text.producer.NumbersAnswerProducer;

@Controller
public class CaptchaController {
	private Logger logger = LoggerFactory.getLogger(getClass());

	protected int _width = 200;

	protected int _height = 50;

	/**
	 * 캡차 생성
	 * @author 정명성
	 * @create date : 2016. 5. 20.
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = "/captcha")
	public void setImages(HttpServletRequest request, HttpServletResponse response) {
		Captcha captcha = new Captcha.Builder(_width, _height)
										.addText(new NumbersAnswerProducer())
										.addBackground(new GradiatedBackgroundProducer())
										.gimp()
										.addNoise()
										.addBorder()
										.build();

		request.getSession().setAttribute("simpleCaptcha", captcha);
		logger.info(request.getSession().getAttribute("simpleCaptcha").toString());
		CaptchaServletUtil.writeImage(response, captcha.getImage());
	}

	/**
	 * 캡차로 생성된 내용을 가지고 오디오 및 정답을 만드는 방법
	 * 
	 * @author 정명성
	 * @create date : 2016. 5. 20.
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = "/captchaAfterAudio")
	public void playCaptchaAudio(HttpServletRequest request, HttpServletResponse response) throws Exception {

		HttpSession session = request.getSession(false);

		Captcha captcha = (Captcha) session.getAttribute("simpleCaptcha");
		CustomTextProducer ctp = new CustomTextProducer(captcha.getAnswer().toCharArray());
		CustomNumberVoiceProducer cvp = new CustomNumberVoiceProducer(captcha.getAnswer().toCharArray(), "kr");

		AudioCaptcha ac = new AudioCaptcha.Builder()
											.addAnswer(ctp)
											.addVoice(cvp)
											.addNoise()
											.build("kr");

		request.getSession().setAttribute("simpleCaptchaAfterAudio", ac);
		CaptchaServletUtil.writeAudio(response, ac.getChallenge());
	}

	/**
	 * 한글로 음성 캡차 만드는 방법
	 * 
	 * @author 정명성
	 * @create date : 2016. 5. 20.
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = "/audio")
	public void playAudio(HttpServletRequest request, HttpServletResponse response) {

		AudioCaptcha ac = new AudioCaptcha.Builder()
											.addAnswer()
											.addNoise()
											.build("kr");

		request.getSession().setAttribute("simpleAudio", ac);
		CaptchaServletUtil.writeAudio(response, ac.getChallenge());
	}
	
	
	/**
	 * 캡차 검증
	 * @author 정명성
	 * @create date : 2016. 5. 20.
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/check/captcha")
	@ResponseBody
	public String checkCaptcha(HttpServletRequest request) {
		HttpSession session = request.getSession(false);

		Captcha captcha = (Captcha) session.getAttribute("simpleCaptcha");
		String answer;
		
		if(request.getParameter("answer") == null) {
			return "Wrong!";
		} else {
			answer = request.getParameter("answer");
		}
		
		if (captcha.isCorrect(answer)) {
			return "Correct!";
		} else {
			return "Wrong!";
		}
	}
}
