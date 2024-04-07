package board.boardservice.controller;

import board.boardservice.controller.form.MemberForm;
import board.boardservice.controller.form.MemberLoginForm;
import board.boardservice.domain.Member;
import board.boardservice.domain.dto.MemberDTO;
import board.boardservice.exception.InvalidCredentialsException;
import board.boardservice.service.MemberService;
import board.boardservice.session.SessionConst;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@Slf4j
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    // 회원가입 폼
    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("memberForm", new MemberForm());

        return "members/create";

    }

    // 회원가입

    @PostMapping("/new")
    public String create(@Valid MemberForm memberForm, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            log.info("에러 발생");

            model.addAttribute("errors", bindingResult.getAllErrors());

            return "members/create";
        }
        Member member = Member.createMember(memberForm);

        try {
            memberService.join(member);

        } catch (IllegalStateException e) {
            bindingResult.reject("globalError", e.getMessage());
            log.info("글로벌 에러");
            return "members/create";
        }


        return "redirect:/members/login";


    }


    //로그인 폼
    @GetMapping("/login")
    public String loginForm(Model model) {
        model.addAttribute("memberLoginForm", new MemberLoginForm());

        return "members/login";
    }
    //로그인 구현 완료

    @PostMapping("/login")
    public String login(@Valid MemberLoginForm memberLoginForm, BindingResult bindingResult, HttpServletRequest request,
                        @RequestParam(defaultValue = "/") String redirectURL) {

        if (bindingResult.hasErrors()) {
            log.info("에러 발생");
            return "members/login";
        }


        try {
            Member loginMember = memberService.login(memberLoginForm.getUsername(), memberLoginForm.getPassword());

            log.info("login? {}", loginMember);

            HttpSession session = request.getSession();

            session.setAttribute(SessionConst.LOGIN_MEMBER, loginMember);

        } catch (InvalidCredentialsException e) {
            bindingResult.reject("loginFail", e.getMessage());

            return "members/login";
        }


        return "redirect:" + redirectURL;

    }

    //로그아웃
    @PostMapping("/logout")
    public String logout(HttpServletRequest request) {

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        return "redirect:/";
    }


    //   회원 정보수정
    @GetMapping("/update")
    public String update(@SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false)
                         Member loginMember, Model model) {

        MemberDTO memberDTO = MemberDTO.createMemberDTO(loginMember);

        model.addAttribute("memberDTO", memberDTO);
        return "members/update";
    }

    //회원 정보수정

    @PostMapping("/update")
    public String update(@Valid @ModelAttribute MemberDTO memberDTO, BindingResult bindingResult,
                         @SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false)
                         Member loginMember) {

        if (bindingResult.hasErrors()) {
            log.info("에러 발생");
            return "members/update";
        }

        if (loginMember == null) {
            log.info("빈 객체");
            return "redirect:/members/login";
        }
        memberService.updateMember(loginMember.getId(), memberDTO);

        return "redirect:/";

    }


}
