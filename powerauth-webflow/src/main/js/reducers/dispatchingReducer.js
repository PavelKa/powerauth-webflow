export default function reducer(state = {currentScreen: "SCREEN_START_HANDSHAKE", context: null}, action) {
    switch (action.type) {
        case "SHOW_SCREEN_LOGIN": {
            return {...state, currentScreen: "SCREEN_LOGIN", context: action.payload};
        }
        case "SHOW_SCREEN_OPERATION_REVIEW": {
            return {
                ...state,
                currentScreen: "SCREEN_OPERATION_REVIEW",
                context: mergeContext(action.type, state.context, action.payload)
            };
        }
        case "SHOW_SCREEN_TOKEN": {
            return {
                ...state,
                currentScreen: "SCREEN_TOKEN",
                context: mergeContext(action.type, state.context, action.payload)
            };
        }
        case "SHOW_SCREEN_SMS": {
            return {
                ...state,
                currentScreen: "SCREEN_SMS",
                context: mergeContext(action.type, state.context, action.payload)
            };
        }
        case "SHOW_SCREEN_SUCCESS": {
            return {...state, currentScreen: "SCREEN_SUCCESS", context: action.payload};
        }
        case "SHOW_SCREEN_ERROR": {
            return {...state, currentScreen: "SCREEN_ERROR", context: action.payload};
        }
    }
    return state;
}

/**
 * Merges old and new context to preserve data related to the operation which should be loaded only once.
 * @param actionType action type
 * @param oldContext old context from which data is taken
 * @param newContext new context into which data is inserted
 * @returns {*} new context
 */
function mergeContext(actionType, oldContext, newContext) {
    if (oldContext === null) {
        // nothing to do
        return newContext;
    }
    switch (actionType) {
        case "SHOW_SCREEN_OPERATION_REVIEW":
            mergeAuthMethods(oldContext, newContext);
            mergeData(oldContext, newContext);
            break;
        case "SHOW_SCREEN_TOKEN":
            mergeData(oldContext, newContext);
            break;
        case "SHOW_SCREEN_SMS":
            mergeData(oldContext, newContext);
            break;
    }
    return newContext;
}

function mergeData(oldContext, newContext) {
    // formData need to remain in context
    if (oldContext.formData !== undefined && newContext.formData === undefined) {
        newContext.formData = oldContext.formData;
    }
    // operation data needs to remain in context
    if (oldContext.data !== undefined && newContext.data === undefined) {
        newContext.data = oldContext.data;
    }
}

function mergeAuthMethods(oldContext, newContext) {
    // authMethods need to remain in context
    if (oldContext.authMethods !== undefined && newContext.authMethods === undefined) {
        newContext.authMethods = oldContext.authMethods;
    }
}
