# frozen_string_literal: true
class Users::AppsController < ApplicationController
  before_action :deny_unknown_user
  before_action :deny_unauthorized_request, only: %i[create]

  def create
    params = create_params

    tempfile = params[:file].try(:tempfile)

    if tempfile.nil?
      return render_error(
        status: :bad_request,
        message: "file is not a filepart"
      )
    end

    response = <<~EOF
    {
      "error": false,
      "results": {
        "name": "com.deploygate.example",
        "package_name": "com.deploygate.example",
        "labels": {},
        "os_name": "Android",
        "path": "/users/____this_is_dummy____/platforms/android/apps/com.deploygate.example",
        "updated_at": 1673597091,
        "version_code": "1",
        "version_name": "1.0",
        "sdk_version": 26,
        "raw_sdk_version": "26",
        "target_sdk_version": 26,
        "signature": null,
        "md5": null,
        "revision": 18,
        "file_size": null,
        "icon": "https://deploygate.com/api/users/____this_is_dummy____/platforms/android/apps/com.deploygate.example/binaries/18/download/icon",
        "message": "",
        "file": "https://deploygate.com/api/users/____this_is_dummy____/platforms/android/apps/com.deploygate.example/binaries/18/download/binary.apk",
        "user": {
          "name": "____this_is_dummy____",
          "profile_icon": "http://example.com"
        }
      }
    }
    EOF

    render(status: :created, json: JSON.parse(response))
  end

  private def create_params
    params.permit(:file, :message, :distribution_key, :distribution_name).tap do |p|
      p.require(:file)
    end
  end

  private def deny_unknown_user
    return if params[:user_id].start_with?("owner_")
    render_error(status: :not_found, message: "#{params[:user_id]} is not found")
  end
end
