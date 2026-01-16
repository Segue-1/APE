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
        DebugLog.log("TriggerConditionPanel constructed");
        DebugLog.log("correctiveCallsSet: " + correctiveCallsSet);
        DebugLog.log("isNegated: " + isNegated);
        DebugLog.log("errorConditions: " + errorConditions);
        DebugLog.log("logicalOperator: " + logicalOperator);
        buildConditionString();
    }

    public void setName () {
        name = "TriggerCondition " + index;
    }

    public void buildConditionString() {
        StringBuilder conditionStringBuilder = new StringBuilder();
        conditionString = "";

        if (errorConditions.size() == 1 && !isNegated.isEmpty()) {
            DebugLog.log("Trigger Condition Builder Error: Mismatching number of error conditions and negations");
            DebugLog.log("errorConditions.size(): " + errorConditions.size());
            DebugLog.log("isNegated.size(): " + isNegated.size());
            DebugLog.log(debugString());
        }
        else if(errorConditions.size() > 1 && logicalOperator.size() != (errorConditions.size() - 1)) {
            DebugLog.log("Trigger Condition Builder Error: Incorrect number of logical operators");
            DebugLog.log("errorConditions.size() not > 1: " + errorConditions.size());
            DebugLog.log("logicalOperator.size() != (errorConditions.size() - 1)");
            DebugLog.log("logicalOperator.size(): " + logicalOperator.size());
            DebugLog.log("errorConditions.size() - 1: " + (errorConditions.size() - 1));
            DebugLog.log(debugString());
        }

        try{
            if (errorConditions.size() == 1) {
                DebugLog.log("errorConditions.size(): " + errorConditions.size());
                try {
                    conditionStringBuilder.append(isNegated.getFirst() ? negation : "").append(errorConditions.getFirst().name);
                }
                catch (Exception e) {
                    DebugLog.log("string builder failed to append");
                    DebugLog.log(e.getMessage());
                }

            }
            else {
                int length = errorConditions.size();

                for (int i = 0; i < length; i++) {
                    conditionStringBuilder.append(isNegated.get(i) ? negation : "");
                    conditionStringBuilder.append(errorConditions.get(i).name + " ");
                    if (i < length - 1) {
                        conditionStringBuilder.append(logicalOperator.get(i));
                    }
                }

            }
        }
        catch (Exception e) {
            DebugLog.log("buildConditionString() errored");
            DebugLog.log(e.getMessage());
        }

        DebugLog.log("conditionString: " +  conditionString);
        this.conditionString = conditionStringBuilder.toString();
    }

    public String debugString() {
        return "TriggerCondition{" +
                "index=" + index +
                ", name='" + name + '\'' +
                ", correctiveCallsSet=" + correctiveCallsSet +
                ", isNegated=" + isNegated +
                ", errorConditions=" + errorConditions +
                ", logicalOperator=" + logicalOperator +
                ", conditionString='" + conditionString + '\'' +
                ", negation='" + negation + '\'' +
                ", and='" + and + '\'' +
                ", or='" + or + '\'' +
                '}';
    }

    @Override
    public String toString() {
        return name;
    }
}
