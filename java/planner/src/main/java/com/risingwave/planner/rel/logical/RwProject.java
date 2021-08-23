package com.risingwave.planner.rel.logical;

import static java.util.Collections.emptyList;

import java.util.List;
import org.apache.calcite.plan.Convention;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelTraitSet;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.convert.ConverterRule;
import org.apache.calcite.rel.core.Project;
import org.apache.calcite.rel.hint.RelHint;
import org.apache.calcite.rel.logical.LogicalProject;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rex.RexNode;
import org.checkerframework.checker.nullness.qual.Nullable;

public class RwProject extends Project implements RisingWaveLogicalRel {
  protected RwProject(
      RelOptCluster cluster,
      RelTraitSet traits,
      List<RelHint> hints,
      RelNode input,
      List<? extends RexNode> projects,
      RelDataType rowType) {
    super(cluster, traits, hints, input, projects, rowType);
    checkConvention();
  }

  @Override
  public RwProject copy(
      RelTraitSet traitSet, RelNode input, List<RexNode> projects, RelDataType rowType) {
    return new RwProject(getCluster(), traitSet, emptyList(), input, projects, rowType);
  }

  public static class RwProjectConverterRule extends ConverterRule {
    public static final RwProject.RwProjectConverterRule INSTANCE =
        Config.INSTANCE
            .withInTrait(Convention.NONE)
            .withOutTrait(LOGICAL)
            .withRuleFactory(RwProjectConverterRule::new)
            .withOperandSupplier(t -> t.operand(LogicalProject.class).anyInputs())
            .withDescription("Converting logical filter to risingwave version.")
            .as(Config.class)
            .toRule(RwProjectConverterRule.class);

    protected RwProjectConverterRule(Config config) {
      super(config);
    }

    @Override
    public @Nullable RelNode convert(RelNode rel) {
      LogicalProject logicalProject = (LogicalProject) rel;
      return new RwProject(
          rel.getCluster(),
          rel.getTraitSet().replace(LOGICAL),
          logicalProject.getHints(),
          logicalProject.getInput(),
          logicalProject.getProjects(),
          logicalProject.getRowType());
    }
  }
}
