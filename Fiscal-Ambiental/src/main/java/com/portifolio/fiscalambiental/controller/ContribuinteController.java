package com.portifolio.fiscalambiental.controller;

import com.portifolio.fiscalambiental.model.Contribuinte;
import com.portifolio.fiscalambiental.service.ContribuinteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contribuintes")
@RequiredArgsConstructor
public class ContribuinteController {

    private final ContribuinteService contribuinteService;

    @GetMapping("/buscar")
    public ResponseEntity<List<Contribuinte>> buscar(@RequestParam(required = false) String termo) {
        return ResponseEntity.ok(contribuinteService.buscar(termo));
    }
}
