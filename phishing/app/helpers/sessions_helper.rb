module SessionsHelper
  def qr_code(code)
    qr_url = "https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=#{code}"
    image_tag(qr_url, class: "center")
  end
end
