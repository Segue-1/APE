import java.util.ArrayList;
import javax.script.*;

public class TriggerCondition {

    public int index;
    public String name;

    public CorrectiveCallsSet correctiveCallsSet;
    public ArrayList<Boolean> isNegated;
    public ArrayList<ErrorCondition> errorConditions;
    public ArrayList<String> logicalOperator;

    public String conditionString;

    private String negationString = "!";
    private String andString = "&&";
    private String orString = "||";


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

        if (!validateInput()){
            return;
        }

        buildConditionString();
    }

    private boolean validateInput() {
        // Check for valid number of logical operators
        if (correctiveCallsSet == null) {
            DebugLog.log("---TriggerCondition Error: null corrective call set passed to TriggerCondition---");
            DebugLog.log(correctiveCallsSet.debugString());
            return false;
        }
        else if (isNegated == null) {
            DebugLog.log("---TriggerCondition Error: null list of negations passed to TriggerCondition---");
            DebugLog.log(isNegated.toString());
            return false;
        }
        else if (errorConditions.isEmpty()) {
            DebugLog.log("---TriggerCondition Error: empty list of error conditions passed to TriggerCondition---");
            DebugLog.log(errorConditions.toString());
            return false;
        }
        else if (errorConditions.size() > 1 && logicalOperator.size() != (errorConditions.size() - 1)) {
            DebugLog.log("---TriggerCondition Error: Incorrect number of logical operators---");
            DebugLog.log("Number of logical operators should be 1 less than the total number of error conditions.");
            DebugLog.log("errorConditions.size(): " + errorConditions.size());
            DebugLog.log("logicalOperator.size(): " + logicalOperator.size());
            DebugLog.log(debugString());
            return false;
        }
        return true;
    }

    public void setName () {
        name = "Trigger Condition " + index;
    }

    public void buildConditionString() {
        StringBuilder conditionStringBuilder = new StringBuilder();
        conditionString = "";

        // How about a validation method instead of putting this here?


        conditionStringBuilder.append(correctiveCallsSet.name + " will be called if ");

        try{
            if (errorConditions.size() == 1) {
                try {
                    conditionStringBuilder.append(isNegated.getFirst() ? negationString : "")
                                          .append(errorConditions.getFirst().name)
                                          .append(" is returned in the response");
                }
                catch (Exception e) {
                    DebugLog.log("string builder failed to append");
                    DebugLog.log(e.getMessage());
                }
            }
            else {
                int length = errorConditions.size();

                for (int i = 0; i < length; i++) {
                    conditionStringBuilder.append(isNegated.get(i) ? negationString : "")
                                          .append(errorConditions.get(i).name + " ");
                    if (i < length - 1) {
                        conditionStringBuilder.append(logicalOperator.get(i) + " ");
                    }
                    else {
                        conditionStringBuilder.append("are returned in the response");
                    }
                }
            }
        }
        catch (Exception e) {
            DebugLog.log("buildConditionString() errored");
            DebugLog.log(e.getMessage());
        }

        this.conditionString = conditionStringBuilder.toString();
        DebugLog.log("conditionString: " +  conditionString);
    }

    // TODO need to write Java to run based off conditionString
    private void buildJavaConditionalStatement() {
        StringBuilder conditionStringBuilder = new StringBuilder();

        boolean isTrue = true;

        conditionStringBuilder.append("if (isTrue) { DebugLog.log(\"TEST SUCCESSFUL!!!\")");

        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("Java");



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
                ", negationString='" + negationString + '\'' +
                ", andString='" + andString + '\'' +
                ", orString='" + orString + '\'' +
                '}';
    }

    @Override
    public String toString() {
        return name;
    }
}
