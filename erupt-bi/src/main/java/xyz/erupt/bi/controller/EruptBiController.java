package xyz.erupt.bi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import xyz.erupt.bi.model.Bi;
import xyz.erupt.bi.model.BiChart;
import xyz.erupt.bi.model.BiDimension;
import xyz.erupt.bi.service.BiService;
import xyz.erupt.bi.view.BiColumn;
import xyz.erupt.bi.view.BiData;
import xyz.erupt.bi.view.BiModel;
import xyz.erupt.bi.view.Reference;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.constant.RestPath;
import xyz.erupt.core.util.SecurityUtil;
import xyz.erupt.tool.EruptDao;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author liyuepeng
 * @date 2019-08-26.
 */
@RestController
@RequestMapping(RestPath.ERUPT_API + "/bi")
public class EruptBiController {

    @Autowired
    private EruptDao eruptDao;

    @Autowired
    private BiService biService;

    @Autowired
    private EntityManager entityManager;

    private static final Integer MAX_SORT = 9999;

    @RequestMapping("/{code}")
    @EruptRouter(verifyType = EruptRouter.VerifyType.MENU, authIndex = 1)
    public BiModel getBuilder(@PathVariable("code") String code) {
        Bi bi = eruptDao.queryEntity(Bi.class, "code = '" + code + "'");
        BiModel biModel = new BiModel();
        biModel.setExport(bi.getExport());
        for (BiChart chart : bi.getBiCharts()) {
            chart.setSqlStatement(null);
            chart.setBi(null);
            if (chart.getSort() == null) {
                chart.setSort(MAX_SORT);
            }
        }
        for (BiDimension dimension : bi.getBiDimension()) {
            dimension.setRefSql(null);
            if (dimension.getSort() == null) {
                dimension.setSort(MAX_SORT);
            }
        }
        biModel.setCode(bi.getCode());
        biModel.setCharts(bi.getBiCharts().stream().sorted(Comparator.comparing(BiChart::getSort)).collect(Collectors.toList()));
        biModel.setDimensions(bi.getBiDimension().stream().sorted(Comparator.comparing(BiDimension::getSort)).collect(Collectors.toList()));
        return biModel;
    }

    @PostMapping("/{code}/data")
    @EruptRouter(verifyType = EruptRouter.VerifyType.MENU, authIndex = 1)
    public BiData getData(@PathVariable("code") String code, @RequestBody Map<String, Object> query) {
        List<Map<String, Object>> list = biService.startQuery(code, query);
        BiData biData = new BiData();
        biData.setList(list);
        if (null != list && list.size() > 0) {
            Set<BiColumn> biColumns = new LinkedHashSet<>();
            Map<String, Object> map = list.get(0);
            for (String key : map.keySet()) {
                BiColumn biColumn = new BiColumn();
                biColumn.setName(key);
                biColumns.add(biColumn);
            }
            biData.setColumns(biColumns);
        }
        return biData;
    }

    @EruptRouter(verifyType = EruptRouter.VerifyType.MENU, authIndex = 1)
    @RequestMapping("{code}/reference/{dim}")
    public List<Reference> refQuery(@PathVariable("code") String code
            , @PathVariable("dim") String dim) {
        BiDimension dimension = eruptDao.queryEntity(BiDimension.class, String.format("code = '%s'", dim));
        List<Map<String, Object>> list = biService.startSqlQuery(dimension.getRefSql(), null);
        List<Reference> references = new ArrayList<>();
        for (Map<String, Object> map : list) {
            if (map.keySet().size() == 1) {
                Object obj = map.values().iterator().next();
                references.add(new Reference(obj, obj));
            } else if (map.keySet().size() >= 2) {
                references.add(new Reference(map.values().iterator().next(),
                        map.values().iterator().next()));
            }
        }
        return references;
    }

    @EruptRouter(verifyType = EruptRouter.VerifyType.MENU, authIndex = 1)
    @RequestMapping("{code}/chart/{chart_code}")
    public Object biChart(@PathVariable("code") String code
            , @PathVariable("chart_code") String chart_code) {
        BiChart chart = eruptDao.queryEntity(BiChart.class, String.format("code = '%s'", chart_code));
        if (chart != null) {
            chart.getSqlStatement();
        }
        return null;
    }


    @EruptRouter(verifyType = EruptRouter.VerifyType.MENU
            , verifyMethod = EruptRouter.VerifyMethod.PARAM, authIndex = 1)
    @RequestMapping("{code}/excel")
    public Object exportExcel(@PathVariable("code") String code, HttpServletRequest request, HttpServletResponse response) {
        if (SecurityUtil.csrfInspect(request, response)) {
            return "非法请求！";
        }

        return null;
    }
}