package net.buildtheearth.terracore.http;


import lombok.experimental.UtilityClass;

/**
 * HTTP response codes.
 *
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Status">MDN documentation</a>
 */
@UtilityClass
final class HttpStatusCodes {

    // Information responses
    final int HTTP_CONTINUE = 100;
    final int HTTP_SWITCHING_PROTOCOLS = 101;
    final int HTTP_PROCESSING = 102;
    final int HTTP_EARLY_HINTS = 103;

    // Successful responses
    final int HTTP_OK = 200;
    final int HTTP_CREATED = 201;
    final int HTTP_ACCEPTED = 202;
    final int HTTP_NON_AUTHORITATIVE_INFORMATION = 203;
    final int HTTP_NO_CONTENT = 204;
    final int HTTP_RESET_CONTENT = 205;
    final int HTTP_PARTIAL_CONTENT = 206;
    final int HTTP_MULTI_STATUS = 207;
    final int HTTP_ALREADY_REPORTED = 208;
    final int HTTP_IM_USED = 209;

    // Redirection message
    final int HTTP_MULTIPLE_CHOICES = 300;
    final int HTTP_MOVED_PERMANENTLY = 301;
    final int HTTP_FOUND = 302;
    final int HTTP_SEE_OTHER = 303;
    final int HTTP_NOT_MODIFIED = 304;
    final int HTTP_USE_PROXY = 305;
    final int HTTP_UNUSED = 306;
    final int HTTP_TEMPORARY_REDIRECT = 307;
    final int HTTP_PERMANENT_REDIRECT = 308;

    // Client error response
    final int HTTP_BAD_REQUEST = 400;
    final int HTTP_UNAUTHORIZED = 401;
    final int HTTP_PAYMENT_REQUIRED = 402;
    final int HTTP_FORBIDDEN = 403;
    final int HTTP_NOT_FOUND = 404;
    final int HTTP_METHOD_NOT_ALLOWED = 405;
    final int HTTP_NOT_ACCEPTABLE = 406;
    final int HTTP_PROXY_AUTHENTICATION_REQUIRED = 407;
    final int HTTP_REQUEST_TIMEOUT = 408;
    final int HTTP_CONFLICT = 409;
    final int HTTP_GONE = 410;
    final int HTTP_LENGTH_REQUIRED = 411;
    final int HTTP_PRECONDITION_FAILED = 412;
    final int HTTP_REQUEST_ENTITY_TOO_LARGE = 413;
    final int HTTP_REQUEST_URI_TOO_LONG = 414;
    final int HTTP_UNSUPPORTED_MEDIA_TYPE = 415;
    final int HTTP_REQUESTED_RANGE_NOT_SATISFIABLE = 416;
    final int HTTP_EXPECTATION_FAILED = 417;
    final int HTTP_I_M_A_TEAPOT = 418;
    final int HTTP_UNPROCESSABLE_ENTITY = 422;
    final int HTTP_LOCKED = 423;
    final int HTTP_FAILED_DEPENDENCY = 424;
    final int HTTP_TOO_EARLY = 425;
    final int HTTP_UPGRADE_REQUIRED = 426;
    final int HTTP_PRECONDITION_REQUIRED = 428;
    final int HTTP_TOO_MANY_REQUESTS = 429;
    final int HTTP_REQUEST_HEADER_FIELDS_TOO_LARGE = 431;
    final int HTTP_UNAVAILABLE_FOR_LEGAL_REASONS = 432;

    // Server error response
    final int HTTP_INTERNAL_SERVER_ERROR = 500;
    final int HTTP_NOT_IMPLEMENTED = 501;
    final int HTTP_BAD_GATEWAY = 502;
    final int HTTP_SERVICE_UNAVAILABLE = 503;
    final int HTTP_GATEWAY_TIMEOUT = 504;
    final int HTTP_VERSION_NOT_SUPPORTED = 505;
    final int HTTP_VARIANT_ALSO_NEGOTIATES = 506;
    final int HTTP_INSUFFICIENT_STORAGE = 507;
    final int HTTP_LOOP_DETECTED = 508;
    final int HTTP_NOT_EXTENDED = 510;
    final int HTTP_NETWORK_AUTHENTICATION_REQUIRED = 511;

}
