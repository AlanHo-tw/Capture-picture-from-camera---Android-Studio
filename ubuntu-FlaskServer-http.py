
import os
from flask import Flask, flash, request, redirect, url_for
from flask import Flask, render_template, send_from_directory, request, jsonify
from werkzeug.utils import secure_filename
app = Flask(__name__)
UPLOAD_FOLDER = 'upload'
app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER  # 設置檔案上傳的目標檔案夾
basedir = os.path.abspath(os.path.dirname(__file__))  # 獲取當前項目的絶對路徑




@app.route('/', methods=['POST'])
def post_file():
    file_dir = os.path.join(basedir, app.config['UPLOAD_FOLDER'])  # 拼接成合法檔案夾地址
    if not os.path.exists(file_dir):
        os.makedirs(file_dir)  # 檔案夾不存在就創建
    # 檢查是否有檔案
    if 'image' in request.files:
        # 讀取檔案
        file = request.files['image']
        # 儲存檔案
        file.save(os.path.join(file_dir, 'received_image.jpg'))
        # 回傳狀態訊息
        print("有收到檔案")
        return '已收到'
    else:
        print("沒有收到檔案")
        return '沒有收到檔案'

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=12345)
