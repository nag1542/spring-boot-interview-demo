package com.interviewprep.platform.service;

import com.interviewprep.platform.domain.Product;
import com.interviewprep.platform.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HeapPressureDemoService {
    private static final int MAX_REPORT_LINES = 200_000;
    private static final int REPORT_PRODUCT_SAMPLE_SIZE = 100;

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public HeapPressureResponse exportProductsWithFindAll(int page, int size) {
        MemorySnapshot before = snapshotMemory();
        Instant startedAt = Instant.now();

        /*
         * PROBLEM PATTERN 1: Loading an entire table into memory.
         *
         * If the products table has 500,000 rows, this loads all of them into JVM heap.
         * Then the stream creates another DTO object for each entity.
         */
        List<Product> products = productRepository.findAll();
        List<ProductExportRow> exportRows = products.stream()
                .map(this::toProductExportRow)
                .toList();
        long processedRows = exportRows.size();

        /*
         * FIX: Use pagination and export one chunk at a time.
         *
         * Uncomment this block and comment the PROBLEM block above.
         * This loads only 'size' rows into memory instead of the whole table.
         */
        // Page<Product> productsPage = productRepository.findAll(PageRequest.of(page, size));
        // List<ProductExportRow> exportRows = productsPage.getContent().stream()
        //         .map(this::toProductExportRow)
        //         .toList();
        // long processedRows = exportRows.size();

        MemorySnapshot after = snapshotMemory();
        return new HeapPressureResponse(
                "PRODUCT_EXPORT_FIND_ALL_LOADS_ENTIRE_TABLE",
                processedRows,
                Duration.between(startedAt, Instant.now()).toMillis(),
                before,
                after);
    }

    @Transactional(readOnly = true)
    public HeapPressureResponse createObjectsInTightLoop(int requestedLines) {
        int safeLineCount = Math.min(requestedLines, MAX_REPORT_LINES);
        List<Product> products = productRepository.findAll(PageRequest.of(0, REPORT_PRODUCT_SAMPLE_SIZE)).getContent();
        if (products.isEmpty()) {
            throw new IllegalStateException("At least one product is required to build report lines");
        }
        MemorySnapshot before = snapshotMemory();
        Instant startedAt = Instant.now();

        /*
         * PROBLEM PATTERN 2: Creating new objects inside a tight loop.
         *
         * This mimics building report lines from database rows.
         * Each iteration creates temporary string objects and retains the final line in memory.
         * With large reports, the allocation rate increases and GC pauses become visible.
         */
        List<String> reportLines = new ArrayList<>();
        for (int i = 0; i < safeLineCount; i++) {
            Product product = products.get(i % products.size());
            String line = "Product #" + product.getId()
                    + " - " + product.getName()
                    + " - " + product.getPrice()
                    + " - stock=" + product.getStock();
            reportLines.add(line);
        }
        long processedRows = reportLines.size();

        /*
         * FIX: Reduce allocation inside the hot loop.
         *
         * Uncomment this block and comment the PROBLEM block above.
         * Pre-size the list and use an explicitly sized StringBuilder for each line.
         * String.format(...) is readable for normal paths, but it is usually heavier in hot loops.
         */
        // List<String> reportLines = new ArrayList<>(safeLineCount);
        // for (int i = 0; i < safeLineCount; i++) {
        //     Product product = products.get(i % products.size());
        //     StringBuilder line = new StringBuilder(96);
        //     line.append("Product #")
        //             .append(product.getId())
        //             .append(" - ")
        //             .append(product.getName())
        //             .append(" - ")
        //             .append(product.getPrice())
        //             .append(" - stock=")
        //             .append(product.getStock());
        //     reportLines.add(line.toString());
        // }
        // long processedRows = reportLines.size();

        MemorySnapshot after = snapshotMemory();
        return new HeapPressureResponse(
                "REPORT_LINE_OBJECT_CREATION_IN_TIGHT_LOOP",
                processedRows,
                Duration.between(startedAt, Instant.now()).toMillis(),
                before,
                after);
    }

    private MemorySnapshot snapshotMemory() {
        Runtime runtime = Runtime.getRuntime();
        long usedBytes = runtime.totalMemory() - runtime.freeMemory();
        return new MemorySnapshot(
                usedBytes,
                runtime.freeMemory(),
                runtime.totalMemory(),
                runtime.maxMemory());
    }

    private ProductExportRow toProductExportRow(Product product) {
        return new ProductExportRow(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getStock());
    }

    private record ProductExportRow(Long id, String name, BigDecimal price, Integer stock) {
    }

    public record HeapPressureResponse(
            String strategy,
            long processedRows,
            long totalDurationMs,
            MemorySnapshot before,
            MemorySnapshot after) {
    }

    public record MemorySnapshot(
            long usedBytes,
            long freeBytes,
            long committedBytes,
            long maxBytes) {
    }
}
