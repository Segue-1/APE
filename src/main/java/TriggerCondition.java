import java.util.ArrayList;

public class TriggerCondition {

    public int index;
    public String name;

    public CorrectiveCallsSet correctiveCallsSet;
    public ArrayList<Boolean> isNegated;
    public ArrayList<ErrorCondition> errorConditions;
    public ArrayList<String> logicalOperator;

    public String conditionString;

    private String negation = "!";
    private String and = "&&";
    private String or = "||";

    // if multiple error conditions, need to store them and logical operator

    TriggerCondition(CorrectiveCallsSet correctiveCallsSet, ArrayList<Boolean> isNegated,
                     ArrayList<ErrorCondition> errorConditions, ArrayList<String> logicalOperator) {
        // logicalOperators are used to join multiple error conditions. If there's only 1 errorCondition, then there
        // are no logicalOperators.
        if (errorConditions.size() == 1) {
            this.correctiveCallsSet = correctiveCallsSet;
            this.isNegated = isNegated;
            this.errorConditions = errorConditions;
            this.logicalOperator = null;
        }
        else {
            this.correctiveCallsSet = correctiveCallsSet;
            this.isNegated = isNegated;
            this.errorConditions = errorConditions;
            this.logicalOperator = logicalOperator;
        }
    }

    public void setName () {
        name = "Set " + index;
    }

    public String buildConditionString() {
        StringBuilder conditionString = new StringBuilder();



        if (errorConditions.size() == 1) {
            return errorConditions.get(0).name;
        }
        else {
            String errorConditionsString = "";
            for (ErrorCondition errorCondition : errorConditions) {
                // barf
            }
        }
        return conditionString.toString();
    }
}
