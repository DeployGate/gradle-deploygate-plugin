class ApplicationController < ActionController::API

  rescue_from ActionController::ParameterMissing, ActionController::UnpermittedParameters do |e|
    render_error(message: e.message, status: :bad_request)
  end

  def deny_unauthorized_request
    return if authorization_token.present?
    render_error(message: "Unauthorized", status: :unauthorized)
  end

  def render_error(message:, status:)
    render(json: { ok: false, message: message }, status: status)
  end

  private def authorization_token
    request.authorization&.split(" ", 2)&.second
  end
end
