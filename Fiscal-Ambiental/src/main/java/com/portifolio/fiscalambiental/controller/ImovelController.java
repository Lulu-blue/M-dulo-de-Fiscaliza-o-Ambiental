package com.portifolio.fiscalambiental.controller;

import com.portifolio.fiscalambiental.model.Imovel;
import com.portifolio.fiscalambiental.service.ImovelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/imoveis")
@RequiredArgsConstructor
public class ImovelController {

    private final ImovelService imovelService;

    @GetMapping("/buscar")
    public ResponseEntity<List<Imovel>> buscar(@RequestParam(required = false) String termo) {
        return ResponseEntity.ok(imovelService.buscar(termo));
    }

    @GetMapping("/por-contribuinte/{contribuinteId}")
    public ResponseEntity<List<Imovel>> buscarPorContribuinte(@PathVariable UUID contribuinteId) {
        return ResponseEntity.ok(imovelService.buscarPorContribuinte(contribuinteId));
    }
}
