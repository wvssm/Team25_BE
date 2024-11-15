package com.team25.backend.domain.reservation.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

// 예약 시 주소 검색 html을 랜더링하는 컨트롤러 입니다.
@Controller
public class AddressController {
    @GetMapping("/address")
    public String roadSearch(){
        return "address/roadSearch";
    }
}
