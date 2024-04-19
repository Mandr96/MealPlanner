package com.mealplanner.demo.RecipeFuzzyService;

import nrc.fuzzy.*;

public class RecipeFuzzySystem {
    private static RecipeFuzzySystem fuzzySystem = null;
    private FuzzyVariable kalDiff;
    private FuzzyVariable healthiness;
    private FuzzyVariable vectorDistance;
    private FuzzyVariable time;
    private FuzzyVariable preferibilita;
    private FuzzyRule highPrefRule, kcalRule, similarVectorRule, timeRule;

    private RecipeFuzzySystem() throws XValuesOutOfOrderException, InvalidFuzzyVariableTermNameException, XValueOutsideUODException, InvalidUODRangeException, InvalidFuzzyVariableNameException, InvalidLinguisticExpressionException, YValueOutOfRangeException {
        kalDiff = new FuzzyVariable("kcal difference", -10000, 10000, "kcal");
        healthiness = new FuzzyVariable("healthiness", -50, 50, "pt");
        vectorDistance = new FuzzyVariable("foodVector", -1, 200);
        time = new FuzzyVariable("tempo", -1, 1000, "minuti");
        preferibilita = new FuzzyVariable("preferibilità", 0, 1);

        kalDiff.addTerm("OK", new TrapezoidFuzzySet(-100, -25, 25, 100));
        healthiness.addTerm("healthy", new LeftLinearFuzzySet(0, 18));
        preferibilita.addTerm("alta", new LeftLinearFuzzySet(0, 1));
        preferibilita.addTerm("bassa", new RightLinearFuzzySet(0, 0.9));
        vectorDistance.addTerm("simili", new RightLinearFuzzySet(25, 200));
        time.addTerm("tanto", new FuzzySet(new double[]{0, 45, 90, 1000}, new double[]{0,0.01,1,1}, 4));
        initRules();
    }

    private void initRules() throws InvalidLinguisticExpressionException {
        highPrefRule = new FuzzyRule();
        highPrefRule.addAntecedent(new FuzzyValue(healthiness, "healthy"));
        highPrefRule.addConclusion(new FuzzyValue(preferibilita, "alta"));

        kcalRule = new FuzzyRule();
        kcalRule.addAntecedent(new FuzzyValue(kalDiff, "OK"));
        kcalRule.addConclusion(new FuzzyValue(preferibilita, "alta"));

        similarVectorRule = new FuzzyRule();
        similarVectorRule.addAntecedent(new FuzzyValue(vectorDistance, "simili"));
        similarVectorRule.addConclusion(new FuzzyValue(preferibilita, "alta"));

        timeRule = new FuzzyRule();
        timeRule.addAntecedent(new FuzzyValue(time, "tanto"));
        timeRule.addConclusion(new FuzzyValue(preferibilita, "bassa"));
    }

    public static RecipeFuzzySystem getFuzzySystem() {
        if(fuzzySystem == null) {
            try {
                fuzzySystem = new RecipeFuzzySystem();
            } catch (XValuesOutOfOrderException | InvalidFuzzyVariableTermNameException | XValueOutsideUODException |
                     InvalidUODRangeException | InvalidFuzzyVariableNameException |
                     InvalidLinguisticExpressionException | YValueOutOfRangeException e) {
                throw new RuntimeException(e);
            }
        }
        return fuzzySystem;
    }

    public double eval(Float kcalDifference, Integer healthiness_pt, Float foodVectorDistance, Integer preparationTime) throws XValuesOutOfOrderException, XValueOutsideUODException, IncompatibleRuleInputsException, InvalidDefuzzifyException, IncompatibleFuzzyValuesException {
        if (kcalDifference > 100F || kcalDifference < -100F)
            kcalDifference = 100F;
        FuzzyValue inputKcalDiff = new FuzzyValue(kalDiff, new TriangleFuzzySet(kcalDifference - 1, kcalDifference, kcalDifference + 1));
        FuzzyValue inputHelthiness = new FuzzyValue(healthiness, new TriangleFuzzySet(healthiness_pt - 0.5, healthiness_pt, healthiness_pt + 0.5));
        FuzzyValue inputVectorDistance = new FuzzyValue(vectorDistance, new TriangleFuzzySet(foodVectorDistance - 0.1F, foodVectorDistance, foodVectorDistance + 0.1F));
        FuzzyValue inputTime = new FuzzyValue(time, new TriangleFuzzySet(preparationTime - 1, preparationTime, preparationTime + 1));

        highPrefRule.removeAllInputs();
        highPrefRule.addInput(inputHelthiness);
        double healthRuleOutput = highPrefRule.execute().fuzzyValueAt(0).momentDefuzzify();

        kcalRule.removeAllInputs();
        kcalRule.addInput(inputKcalDiff);
        double kcalRuleOutput = kcalRule.execute().fuzzyValueAt(0).momentDefuzzify();

        similarVectorRule.removeAllInputs();
        similarVectorRule.addInput(inputVectorDistance);
        double vectorRuleOutput = similarVectorRule.execute().fuzzyValueAt(0).momentDefuzzify();

        timeRule.removeAllInputs();
        timeRule.addInput(inputTime);
        double timeRuleOutput = timeRule.execute().fuzzyValueAt(0).momentDefuzzify();

        return (healthRuleOutput * 2 + kcalRuleOutput * 3 + vectorRuleOutput * 2 + timeRuleOutput) / 8;
    }
}
